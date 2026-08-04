package com.airproject.airproject.controller;

import com.airproject.airproject.dto.UpdatePreferencesRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void getOwnProfile_ReturnsPreferences() {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.REGISTERED_USER)
                .active(true)
                .preferredTheme("DARK")
                .preferredLanguage("es")
                .build();

        when(userService.getCurrentUser("user@example.com")).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getOwnProfile(userDetails);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("DARK", result.getBody().getPreferredTheme());
        assertEquals("es", result.getBody().getPreferredLanguage());
    }

    @Test
    void updateOwnPreferences_ReturnsUpdatedProfile() {
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
                .preferredTheme("LIGHT")
                .preferredLanguage("en")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .preferredTheme("LIGHT")
                .preferredLanguage("en")
                .build();

        when(userService.updateUserPreferences(eq("user@example.com"), any(UpdatePreferencesRequest.class))).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.updateOwnPreferences(userDetails, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("LIGHT", result.getBody().getPreferredTheme());
        assertEquals("en", result.getBody().getPreferredLanguage());
    }
}
