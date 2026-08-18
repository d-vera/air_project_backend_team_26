package com.airproject.airproject.service;

import com.airproject.airproject.dto.PreferenceResponse;
import com.airproject.airproject.dto.UpdatePreferenceRequest;
import com.airproject.airproject.exception.UserNotFoundException;
import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Theme;
import com.airproject.airproject.model.User;
import com.airproject.airproject.model.UserPreference;
import com.airproject.airproject.repository.UserPreferenceRepository;
import com.airproject.airproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository, UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PreferenceResponse getPreferencesByUserEmail(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserPreference preference = preferenceRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultPreference(user));

        return PreferenceResponse.fromEntity(preference);
    }

    @Transactional
    public PreferenceResponse updatePreferences(String email, UpdatePreferenceRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserPreference preference = preferenceRepository.findByUserEmail(email)
                .orElseGet(() -> createDefaultPreference(user));

        if (request.getLanguage() != null) {
            preference.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            preference.setTheme(request.getTheme());
        }

        UserPreference updated = preferenceRepository.save(preference);
        return PreferenceResponse.fromEntity(updated);
    }

    @Transactional
    public UserPreference createDefaultPreference(User user) {
        UserPreference preference = UserPreference.builder()
                .user(user)
                .language(Language.ES)
                .theme(Theme.SYSTEM)
                .build();
        return preferenceRepository.save(preference);
    }
}
