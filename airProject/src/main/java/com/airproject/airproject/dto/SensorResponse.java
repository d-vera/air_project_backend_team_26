package com.airproject.airproject.dto;

import com.airproject.airproject.model.Sensor;
import com.airproject.airproject.model.SensorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorResponse {

    private Integer id;
    private String uidSensor;
    private String name;
    private String sensorType;
    private Double latitude;
    private Double longitude;
    private String firmwareVersion;
    private SensorStatus sensorStatus;
    private Instant lastSeen;
    private Long userId;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static SensorResponse fromEntity(Sensor sensor) {
        if (sensor == null) {
            return null;
        }
        return SensorResponse.builder()
                .id(sensor.getId())
                .uidSensor(sensor.getUidSensor())
                .name(sensor.getName())
                .sensorType(sensor.getSensorType())
                .latitude(sensor.getLatitude())
                .longitude(sensor.getLongitude())
                .firmwareVersion(sensor.getFirmwareVersion())
                .sensorStatus(sensor.getSensorStatus())
                .lastSeen(sensor.getLastSeen())
                .userId(sensor.getUser() != null ? sensor.getUser().getId() : null)
                .active(sensor.getActive())
                .createdAt(sensor.getCreatedAt())
                .updatedAt(sensor.getUpdatedAt())
                .build();
    }
}
