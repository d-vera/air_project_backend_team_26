package com.airproject.airproject.service;

import com.airproject.airproject.model.Sensor;
import com.airproject.airproject.model.SensorStatus;
import com.airproject.airproject.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SensorStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SensorStatusScheduler.class);

    private final SensorRepository sensorRepository;

    @Value("${sensor.offline.threshold-minutes:60}")
    private long thresholdMinutes;

    public SensorStatusScheduler(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    /**
     * Periodically checks for stale sensors (ONLINE but no data received within
     * the configured threshold) and marks them as OFFLINE.
     * Sensors in MAINTENANCE mode are never touched.
     * Runs with a fixed delay of 5 minutes (300000 ms) between executions.
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void markStaleSensorsOffline() {
        Instant cutoff = Instant.now().minus(thresholdMinutes, ChronoUnit.MINUTES);

        List<Sensor> staleSensors = sensorRepository
                .findByActiveTrueAndSensorStatusAndLastSeenBefore(SensorStatus.ONLINE, cutoff);

        if (staleSensors.isEmpty()) {
            logger.debug("No stale sensors found to mark as OFFLINE");
            return;
        }

        for (Sensor sensor : staleSensors) {
            sensor.setSensorStatus(SensorStatus.OFFLINE);
            sensorRepository.save(sensor);
            logger.info("Auto-marked sensor as OFFLINE: id={}, uid={}, lastSeen={}",
                    sensor.getId(), sensor.getUidSensor(), sensor.getLastSeen());
        }

        logger.info("Marked {} stale sensor(s) as OFFLINE", staleSensors.size());
    }
}
