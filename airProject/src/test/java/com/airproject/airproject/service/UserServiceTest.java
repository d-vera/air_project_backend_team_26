package com.airproject.airproject.service;

import com.airproject.airproject.dto.UserResponse;
import com.airproject.airproject.model.Role;
import com.airproject.airproject.model.User;
import com.airproject.airproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.REGISTERED_USER)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getCurrentUser_ReturnsUserResponse() {
        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getCurrentUser("test@example.com");

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
    }
}
