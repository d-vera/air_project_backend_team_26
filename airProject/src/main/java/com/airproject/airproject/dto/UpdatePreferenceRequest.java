package com.airproject.airproject.dto;

import com.airproject.airproject.model.Language;
import com.airproject.airproject.model.Theme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating user preferences")
public class UpdatePreferenceRequest {

    @Schema(description = "Application language (ES or EN)", example = "ES")
    private Language language;

    @Schema(description = "UI theme mode (DARK, LIGHT, or SYSTEM)", example = "SYSTEM", defaultValue = "SYSTEM")
    private Theme theme;

    @Schema(description = "Active preference status flag", example = "true")
    private Boolean active;
}
