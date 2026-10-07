package com.airproject.airproject.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtTokenProvider tokenProvider,
                                    TokenBlacklist tokenBlacklist,
                                    CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.tokenBlacklist = tokenBlacklist;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                logger.warn("WebSocket CONNECT rejected: missing or malformed Authorization header");
                throw new BadCredentialsException("Missing or malformed Authorization header");
            }

            String token = authHeader.substring(7);

            if (!tokenProvider.validateToken(token)) {
                logger.warn("WebSocket CONNECT rejected: invalid or expired JWT token");
                throw new BadCredentialsException("Invalid or expired JWT token");
            }

            String jti = tokenProvider.getJtiFromToken(token);
            if (tokenBlacklist.isBlacklisted(jti)) {
                logger.warn("WebSocket CONNECT rejected: token has been blacklisted (jti={})", jti);
                throw new BadCredentialsException("Token has been revoked");
            }

            String email = tokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!userDetails.isEnabled()) {
                logger.warn("WebSocket CONNECT rejected: user account is disabled for {}", email);
                throw new BadCredentialsException("User account is disabled");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            if (accessor.isMutable()) {
                accessor.setUser(authentication);
                logger.info("WebSocket CONNECT authenticated successfully for user: {}", email);
                return message;
            } else {
                StompHeaderAccessor mutableAccessor = StompHeaderAccessor.wrap(message);
                mutableAccessor.setUser(authentication);
                logger.info("WebSocket CONNECT authenticated successfully for user: {}", email);
                return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
            }
        }

        return message;
    }
}
