package com.airproject.airproject.dto;

import com.airproject.airproject.model.SensorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSensorRequest {

    private String name;

    private String sensorType;

    private Double latitude;

    private Double longitude;

    private String firmwareVersion;

    private SensorStatus sensorStatus;

    private Long userId;
}
