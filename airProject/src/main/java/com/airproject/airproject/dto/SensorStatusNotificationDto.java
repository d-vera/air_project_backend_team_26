package com.airproject.airproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorStatusNotificationDto {

    private Integer sensorId;
    private String uidSensor;
    private String name;
    private String previousStatus;
    private String newStatus;
    private Boolean active;
    private Instant lastSeen;
}
