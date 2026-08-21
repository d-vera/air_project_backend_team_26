package com.airproject.airproject.service;

import com.airproject.airproject.dto.UpdateUserRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private User inactiveUser;

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

        inactiveUser = User.builder()
                .id(2L)
                .email("inactive@example.com")
                .password("encodedPassword")
                .firstName("Inactive")
                .lastName("User")
                .role(Role.REGISTERED_USER)
                .active(false)
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

    @Test
    void getAllUsers_ReturnsActiveAndInactiveUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, inactiveUser));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(2, responses.size());
        assertTrue(responses.get(0).isActive());
        assertFalse(responses.get(1).isActive());
        assertEquals("inactive@example.com", responses.get(1).getEmail());
    }

    @Test
    void getUserById_ReturnsInactiveUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));

        UserResponse response = userService.getUserById(2L);

        assertNotNull(response);
        assertEquals("inactive@example.com", response.getEmail());
        assertFalse(response.isActive());
    }

    @Test
    void updateUser_ReactivatesInactiveUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = UpdateUserRequest.builder()
                .active(true)
                .build();

        UserResponse response = userService.updateUser(2L, request);

        assertNotNull(response);
        assertTrue(response.isActive());
        assertEquals("inactive@example.com", response.getEmail());
    }
}
