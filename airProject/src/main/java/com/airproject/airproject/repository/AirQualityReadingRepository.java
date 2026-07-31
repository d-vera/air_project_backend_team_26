package com.airproject.airproject.repository;

import com.airproject.airproject.dto.AggregatedReading;
import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.model.AirQualityReadingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AirQualityReadingRepository extends JpaRepository<AirQualityReading, AirQualityReadingId> {

    /**
     * Aggregates air quality readings using TimescaleDB time_bucket().
     * Groups by the specified interval and device_id, computing AVG for all metrics.
     * When deviceId is null, returns data for all devices.
     */
    @Query(value = """
            SELECT time_bucket(CAST(:interval AS INTERVAL), time) AS bucket,
                   device_id AS deviceId,
                   AVG(temperature) AS avgTemperature,
                   AVG(humidity) AS avgHumidity,
                   AVG(co2) AS avgCo2,
                   AVG(pm1_0) AS avgPm1_0,
                   AVG(pm2_5) AS avgPm2_5,
                   AVG(pm10) AS avgPm10
            FROM air_quality_readings
            WHERE time >= :fromTime AND time < :toTime
              AND (:deviceId IS NULL OR device_id = :deviceId)
            GROUP BY bucket, device_id
            ORDER BY bucket ASC, device_id ASC
            """, nativeQuery = true)
    List<AggregatedReading> findAggregatedReadings(
            @Param("interval") String interval,
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            @Param("deviceId") String deviceId
    );

    /**
     * Returns the latest reading for each device using PostgreSQL DISTINCT ON.
     * When deviceId is null, returns the latest reading for all devices.
     */
    @Query(value = """
            SELECT DISTINCT ON (device_id) *
            FROM air_quality_readings
            WHERE (:deviceId IS NULL OR device_id = :deviceId)
            ORDER BY device_id, time DESC
            """, nativeQuery = true)
    List<AirQualityReading> findLatestReadingPerDevice(@Param("deviceId") String deviceId);
}
