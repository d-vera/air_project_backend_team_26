package com.airproject.airproject.service;

import com.airproject.airproject.dto.PreferenceResponse;
import com.airproject.airproject.dto.UpdatePreferenceRequest;
import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Role;
import com.airproject.airproject.model.Theme;
import com.airproject.airproject.model.User;
import com.airproject.airproject.model.UserPreference;
import com.airproject.airproject.repository.UserPreferenceRepository;
import com.airproject.airproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPreferenceService preferenceService;

    private User testUser;
    private UserPreference testPreference;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.REGISTERED_USER)
                .active(true)
                .build();

        testPreference = UserPreference.builder()
                .id(10L)
                .user(testUser)
                .language(Language.ES)
                .theme(Theme.SYSTEM)
                .active(true)
                .build();
    }

    @Test
    void getPreferencesByUserEmail_ReturnsPreferenceResponse() {
        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testPreference));

        PreferenceResponse response = preferenceService.getPreferencesByUserEmail("test@example.com");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(Language.ES, response.getLanguage());
        assertEquals(Theme.SYSTEM, response.getTheme());
    }

    @Test
    void updatePreferences_UpdatesThemeAndLanguage() {
        when(userRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePreferenceRequest request = UpdatePreferenceRequest.builder()
                .language(Language.EN)
                .theme(Theme.LIGHT)
                .build();

        PreferenceResponse response = preferenceService.updatePreferences("test@example.com", request);

        assertNotNull(response);
        assertEquals(Language.EN, response.getLanguage());
        assertEquals(Theme.LIGHT, response.getTheme());
        verify(preferenceRepository).save(testPreference);
    }
}
