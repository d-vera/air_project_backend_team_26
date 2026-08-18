package com.airproject.airproject.service;

import com.airproject.airproject.dto.AuthResponse;
import com.airproject.airproject.dto.LoginRequest;
import com.airproject.airproject.dto.RegisterRequest;
import com.airproject.airproject.exception.EmailAlreadyExistsException;
import com.airproject.airproject.exception.InvalidCredentialsException;
import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Role;
import com.airproject.airproject.model.Theme;
import com.airproject.airproject.model.User;
import com.airproject.airproject.model.UserPreference;
import com.airproject.airproject.repository.UserRepository;
import com.airproject.airproject.security.JwtTokenProvider;
import com.airproject.airproject.security.TokenBlacklist;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklist = tokenBlacklist;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.REGISTERED_USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        UserPreference preference = UserPreference.builder()
                .user(savedUser)
                .language(Language.ES)
                .theme(Theme.SYSTEM)
                .build();
        savedUser.setPreference(preference);
        userRepository.save(savedUser);

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (tokenProvider.validateToken(token)) {
                String jti = tokenProvider.getJtiFromToken(token);
                tokenBlacklist.blacklist(jti);
            }
        }
    }
}
