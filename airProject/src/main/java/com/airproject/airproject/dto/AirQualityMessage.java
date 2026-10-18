package com.airproject.airproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirQualityMessage(
        @JsonProperty("device") Device device,
        @JsonProperty("environment") Environment environment,
        @JsonProperty("airData") AirData airData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Device(
            @JsonProperty("id") String id,
            @JsonProperty("firmware") String firmware,
            @JsonProperty("timestamp") Long timestamp
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Environment(
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("humidity") Double humidity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AirData(
            @JsonProperty("co2") Double co2,
            @JsonProperty("pm1_0") Double pm1_0,
            @JsonProperty("pm2_5") Double pm2_5,
            @JsonProperty("pm10") Double pm10
    ) {}
}
