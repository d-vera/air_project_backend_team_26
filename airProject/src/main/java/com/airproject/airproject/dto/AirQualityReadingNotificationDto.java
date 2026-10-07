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
public class AirQualityReadingNotificationDto {

    private String action;
    private Long id;
    private String sensorUid;
    private String deviceName;
    private Instant timestamp;
    private Double temperature;
    private Double humidity;
    private Double co2;
    private Double pm10Small;
    private Double pm25;
    private Double pm10;
}
