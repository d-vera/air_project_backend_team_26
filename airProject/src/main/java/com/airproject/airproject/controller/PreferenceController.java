package com.airproject.airproject.controller;

import com.airproject.airproject.dto.PreferenceResponse;
import com.airproject.airproject.dto.UpdatePreferenceRequest;
import com.airproject.airproject.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "User Preferences", description = "Endpoints for managing user UI preferences")
public class PreferenceController {

    private final UserPreferenceService preferenceService;

    public PreferenceController(UserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get own preferences", description = "Retrieves UI preferences of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PreferenceResponse> getOwnPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        PreferenceResponse response = preferenceService.getPreferencesByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update own preferences", description = "Updates UI preferences (language, theme) of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid field values"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PreferenceResponse> updateOwnPreferences(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @Valid @RequestBody UpdatePreferenceRequest request) {
        PreferenceResponse response = preferenceService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
