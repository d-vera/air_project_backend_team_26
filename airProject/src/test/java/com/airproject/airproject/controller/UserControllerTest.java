package com.airproject.airproject.controller;

import com.airproject.airproject.dto.UserResponse;
import com.airproject.airproject.model.Role;
import com.airproject.airproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
    }

    @Test
    void getOwnProfile_ReturnsUserProfile() {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.REGISTERED_USER)
                .active(true)
                .build();

        when(userService.getCurrentUser("user@example.com")).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getOwnProfile(userDetails);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("user@example.com", result.getBody().getEmail());
        assertEquals("John", result.getBody().getFirstName());
    }
}
