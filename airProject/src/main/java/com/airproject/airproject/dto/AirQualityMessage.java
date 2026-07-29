package com.airproject.airproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AirQualityMessage(
        @JsonProperty("dispositivo") Dispositivo dispositivo,
        @JsonProperty("entorno") Entorno entorno,
        @JsonProperty("aire") Aire aire
) {
    public record Dispositivo(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("firmware") String firmware,
            @JsonProperty("secuencia") Integer secuencia,
            @JsonProperty("Timestamp") Long timestamp
    ) {}

    public record Entorno(
            @JsonProperty("temperatura") Double temperatura,
            @JsonProperty("humedad") Double humedad
    ) {}

    public record Aire(
            @JsonProperty("co2") Double co2,
            @JsonProperty("pm1_0") Double pm1_0,
            @JsonProperty("pm2_5") Double pm2_5,
            @JsonProperty("pm10") Double pm10
    ) {}
}
