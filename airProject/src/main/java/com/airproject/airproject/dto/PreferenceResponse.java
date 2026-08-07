package com.airproject.airproject.dto;

import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Theme;
import com.airproject.airproject.model.UserPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User preference response object")
public class PreferenceResponse {

    @Schema(description = "Preference ID", example = "1")
    private Long id;

    @Schema(description = "Preferred application language", example = "ES")
    private Language language;

    @Schema(description = "Preferred UI theme mode (DARK, LIGHT, or SYSTEM)", example = "SYSTEM", defaultValue = "SYSTEM")
    private Theme theme;

    @Schema(description = "Active preference status flag", example = "true")
    private Boolean active;

    public static PreferenceResponse fromEntity(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        return PreferenceResponse.builder()
                .id(preference.getId())
                .language(preference.getLanguage())
                .theme(preference.getTheme())
                .active(preference.getActive())
                .build();
    }
}
