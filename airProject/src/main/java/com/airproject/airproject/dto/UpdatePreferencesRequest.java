package com.airproject.airproject.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {

    @Pattern(regexp = "^(?i)(DARK|LIGHT)$", message = "preferredTheme must be either 'DARK' or 'LIGHT'")
    private String preferredTheme;

    @Pattern(regexp = "^(?i)(en|es)$", message = "preferredLanguage must be either 'en' or 'es'")
    private String preferredLanguage;
}
