package com.airproject.airproject.dto;

import java.time.Instant;

/**
 * Spring Data projection interface for mapping native time_bucket() query results.
 */
public interface AggregatedReading {

    Instant getBucket();

    String getDeviceId();

    Double getAvgTemperature();

    Double getAvgHumidity();

    Double getAvgCo2();

    Double getAvgPm1_0();

    Double getAvgPm2_5();

    Double getAvgPm10();
}
