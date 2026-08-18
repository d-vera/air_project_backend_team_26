package com.airproject.airproject.controller;

import com.airproject.airproject.dto.PreferenceResponse;
import com.airproject.airproject.dto.UpdatePreferenceRequest;
import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Theme;
import com.airproject.airproject.service.UserPreferenceService;
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
class PreferenceControllerTest {

    @Mock
    private UserPreferenceService preferenceService;

    @InjectMocks
    private PreferenceController preferenceController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
    }

    @Test
    void getOwnPreferences_ReturnsPreferenceResponse() {
        PreferenceResponse response = PreferenceResponse.builder()
                .id(1L)
                .language(Language.ES)
                .theme(Theme.DARK)
                .build();

        when(preferenceService.getPreferencesByUserEmail("user@example.com")).thenReturn(response);

        ResponseEntity<PreferenceResponse> result = preferenceController.getOwnPreferences(userDetails);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(Theme.DARK, result.getBody().getTheme());
        assertEquals(Language.ES, result.getBody().getLanguage());
    }

    @Test
    void updateOwnPreferences_ReturnsUpdatedPreferenceResponse() {
        UpdatePreferenceRequest request = UpdatePreferenceRequest.builder()
                .theme(Theme.SYSTEM)
                .language(Language.EN)
                .build();

        PreferenceResponse response = PreferenceResponse.builder()
                .id(1L)
                .language(Language.EN)
                .theme(Theme.SYSTEM)
                .build();

        when(preferenceService.updatePreferences(eq("user@example.com"), any(UpdatePreferenceRequest.class))).thenReturn(response);

        ResponseEntity<PreferenceResponse> result = preferenceController.updateOwnPreferences(userDetails, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(Theme.SYSTEM, result.getBody().getTheme());
        assertEquals(Language.EN, result.getBody().getLanguage());
    }
}
