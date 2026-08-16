package com.airproject.airproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSensorRequest {

    @NotBlank(message = "UID sensor is required")
    private String uidSensor;

    @NotBlank(message = "Sensor name is required")
    private String name;

    private String sensorType;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String firmwareVersion;

    private Long userId;
}
