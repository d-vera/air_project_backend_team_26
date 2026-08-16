package com.airproject.airproject.service;

import com.airproject.airproject.dto.*;
import com.airproject.airproject.dto.HistoricalDataResponse.AggregatedReadingDto;
import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.repository.AirQualityReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityService.class);

    private final AirQualityReadingRepository repository;
    private final SensorService sensorService;

    public AirQualityService(AirQualityReadingRepository repository, SensorService sensorService) {
        this.repository = repository;
        this.sensorService = sensorService;
    }

    @Transactional
    public AirQualityReading processAndSave(AirQualityMessage message, String topic) {
        String deviceId = message.dispositivo() != null ? message.dispositivo().id() : "unknown";
        String deviceName = message.dispositivo() != null ? message.dispositivo().nombre() : null;
        String firmware = message.dispositivo() != null ? message.dispositivo().firmware() : null;
        Integer sequence = message.dispositivo() != null ? message.dispositivo().secuencia() : null;
        Long timestamp = message.dispositivo() != null ? message.dispositivo().timestamp() : null;

        Instant readingTime;
        if (timestamp != null && timestamp > 0) {
            readingTime = timestamp > 1_000_000_000_000L ? Instant.ofEpochMilli(timestamp) : Instant.ofEpochSecond(timestamp);
        } else {
            readingTime = Instant.now();
        }

        Double temperature = message.entorno() != null ? message.entorno().temperatura() : null;
        Double humidity = message.entorno() != null ? message.entorno().humedad() : null;

        Double co2 = message.aire() != null ? message.aire().co2() : null;
        Double pm10Small = message.aire() != null ? message.aire().pm1_0() : null;
        Double pm25 = message.aire() != null ? message.aire().pm2_5() : null;
        Double pm10 = message.aire() != null ? message.aire().pm10() : null;

        AirQualityReading reading = new AirQualityReading(
                readingTime,
                deviceId,
                deviceName,
                firmware,
                sequence,
                topic,
                temperature,
                humidity,
                co2,
                pm10Small,
                pm25,
                pm10
        );

        AirQualityReading savedReading = repository.save(reading);
        logger.info("Successfully ingested air quality reading from device: {} on topic: {}", deviceId, topic);

        try {
            sensorService.updateSensorStatusFromReading(deviceId, firmware, readingTime);
        } catch (Exception e) {
            logger.warn("Failed to update sensor status for device {}: {}", deviceId, e.getMessage());
        }

        return savedReading;
    }

    // ==================== Historical Data Methods ====================

    /**
     * Resolves a TimeRange enum to concrete from/to Instant values.
     */
    public Instant[] resolveTimeRange(TimeRange range) {
        Instant now = Instant.now();
        Instant from;
        switch (range) {
            case LAST_DAY:
                from = now.minus(1, ChronoUnit.DAYS);
                break;
            case LAST_WEEK:
                from = now.minus(7, ChronoUnit.DAYS);
                break;
            case LAST_MONTH:
                from = now.minus(31, ChronoUnit.DAYS);
                break;
            case LAST_YEAR:
                from = now.minus(365, ChronoUnit.DAYS);
                break;
            default:
                throw new IllegalArgumentException("Unknown time range: " + range);
        }
        return new Instant[]{from, now};
    }

    /**
     * Calculates the aggregation interval string based on the duration of the range.
     * ≤ 1 day   → '10 minutes'
     * ≤ 1 week  → '1 hour'
     * ≤ 1 month → '12 hours'
     * else      → '24 hours'
     */
    public String calculateInterval(Instant from, Instant to) {
        Duration duration = Duration.between(from, to);
        long days = duration.toDays();

        if (days <= 1) {
            return "10 minutes";
        } else if (days <= 7) {
            return "1 hour";
        } else if (days <= 31) {
            return "12 hours";
        } else {
            return "24 hours";
        }
    }

    /**
     * Retrieves historical air quality data with time-bucket aggregation.
     * Enforces access control: unauthenticated users cannot use LAST_YEAR or custom ranges.
     *
     * @throws org.springframework.security.access.AccessDeniedException if visitor attempts restricted access
     * @throws IllegalArgumentException if parameters are invalid
     */
    @Transactional(readOnly = true)
    public HistoricalDataResponse getHistoricalData(TimeRange range, Instant from, Instant to, String deviceId) {
        // Validate mutual exclusivity
        if (range != null && (from != null || to != null)) {
            throw new IllegalArgumentException("Parameters 'range' and 'from/to' are mutually exclusive. Use one or the other.");
        }
        if (range == null && (from == null || to == null)) {
            throw new IllegalArgumentException("Either 'range' or both 'from' and 'to' must be provided.");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Parameter 'from' must be before 'to'.");
        }

        // Check authentication for restricted operations
        boolean isAuthenticated = isUserAuthenticated();

        if (!isAuthenticated) {
            if (range == TimeRange.LAST_YEAR) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Authentication required to access LAST_YEAR range.");
            }
            if (range == null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Authentication required to use custom date ranges.");
            }
        }

        // Resolve range to from/to
        Instant resolvedFrom;
        Instant resolvedTo;
        if (range != null) {
            Instant[] resolved = resolveTimeRange(range);
            resolvedFrom = resolved[0];
            resolvedTo = resolved[1];
        } else {
            resolvedFrom = from;
            resolvedTo = to;
        }

        // Calculate aggregation interval
        String interval = calculateInterval(resolvedFrom, resolvedTo);

        logger.info("Querying historical data: from={}, to={}, interval={}, deviceId={}",
                resolvedFrom, resolvedTo, interval, deviceId);

        // Execute query
        List<AggregatedReading> results = repository.findAggregatedReadings(
                interval, resolvedFrom, resolvedTo, deviceId);

        // Map projections to DTOs
        List<AggregatedReadingDto> data = results.stream()
                .map(AggregatedReadingDto::fromProjection)
                .toList();

        return new HistoricalDataResponse(
                new HistoricalDataResponse.Range(resolvedFrom, resolvedTo),
                interval,
                data
        );
    }

    /**
     * Retrieves the latest reading per device.
     */
    @Transactional(readOnly = true)
    public CurrentReadingResponse getCurrentReadings(String deviceId) {
        logger.info("Querying current readings: deviceId={}", deviceId);

        List<AirQualityReading> latestReadings = repository.findLatestReadingPerDevice(deviceId);

        List<CurrentReadingResponse.DeviceReading> readings = latestReadings.stream()
                .map(CurrentReadingResponse.DeviceReading::fromEntity)
                .toList();

        return new CurrentReadingResponse(readings);
    }

    /**
     * Checks if the current request is from an authenticated user.
     */
    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}

