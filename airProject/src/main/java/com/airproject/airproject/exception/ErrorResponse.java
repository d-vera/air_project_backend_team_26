package com.airproject.airproject.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Builder.Default
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;
}
