package com.airproject.airproject.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private CustomUserDetailsService userDetailsService;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(tokenProvider, tokenBlacklist, userDetailsService);
    }

    @Test
    void preSend_WithValidToken_ShouldAuthenticateSuccessfully() {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        String jti = "jwt-uuid-123";

        UserDetails userDetails = new User(email, "password", true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getJtiFromToken(token)).thenReturn(jti);
        when(tokenBlacklist.isBlacklisted(jti)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertNotNull(resultAccessor.getUser());
        assertInstanceOf(Authentication.class, resultAccessor.getUser());
        assertEquals(email, ((Authentication) resultAccessor.getUser()).getName());
    }

    @Test
    void preSend_WithMissingAuthHeader_ShouldThrowBadCredentialsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_WithMalformedAuthHeader_ShouldThrowBadCredentialsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Basic some-credentials");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_WithInvalidToken_ShouldThrowBadCredentialsException() {
        String token = "invalid.token";
        when(tokenProvider.validateToken(token)).thenReturn(false);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_WithBlacklistedToken_ShouldThrowBadCredentialsException() {
        String token = "blacklisted.token";
        String jti = "revoked-jti";

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getJtiFromToken(token)).thenReturn(jti);
        when(tokenBlacklist.isBlacklisted(jti)).thenReturn(true);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_WithDisabledUser_ShouldThrowBadCredentialsException() {
        String token = "disabled.user.token";
        String email = "disabled@example.com";
        String jti = "disabled-jti";

        UserDetails userDetails = new User(email, "password", false, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getJtiFromToken(token)).thenReturn(jti);
        when(tokenBlacklist.isBlacklisted(jti)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_NonConnectCommand_ShouldPassThroughWithoutAuth() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/readings");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);
        assertNotNull(result);
        verifyNoInteractions(tokenProvider, tokenBlacklist, userDetailsService);
    }
}
