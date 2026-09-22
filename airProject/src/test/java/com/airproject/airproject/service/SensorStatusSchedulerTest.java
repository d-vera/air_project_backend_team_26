package com.airproject.airproject.service;

import com.airproject.airproject.model.Sensor;
import com.airproject.airproject.model.SensorStatus;
import com.airproject.airproject.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorStatusSchedulerTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorStatusScheduler sensorStatusScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sensorStatusScheduler, "thresholdMinutes", 60L);
    }

    @Test
    void markStaleSensorsOffline_whenStaleSensorsExist_shouldMarkThemOffline() {
        Sensor staleSensor = Sensor.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .name("Stale Sensor")
                .sensorStatus(SensorStatus.ONLINE)
                .lastSeen(Instant.now().minus(2, ChronoUnit.HOURS))
                .active(true)
                .build();

        when(sensorRepository.findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.ONLINE), any(Instant.class)))
                .thenReturn(List.of(staleSensor));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(inv -> inv.getArgument(0));

        sensorStatusScheduler.markStaleSensorsOffline();

        verify(sensorRepository).save(staleSensor);
        assert staleSensor.getSensorStatus() == SensorStatus.OFFLINE;
    }

    @Test
    void markStaleSensorsOffline_whenNoStaleSensors_shouldDoNothing() {
        when(sensorRepository.findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.ONLINE), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        sensorStatusScheduler.markStaleSensorsOffline();

        verify(sensorRepository, never()).save(any(Sensor.class));
    }

    @Test
    void markStaleSensorsOffline_shouldNotQueryMaintenanceSensors() {
        // The query only looks for ONLINE sensors, so MAINTENANCE sensors are never returned
        when(sensorRepository.findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.ONLINE), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        sensorStatusScheduler.markStaleSensorsOffline();

        // Verify the query was called with SensorStatus.ONLINE, not MAINTENANCE
        verify(sensorRepository).findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.ONLINE), any(Instant.class));
        verify(sensorRepository, never()).findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.MAINTENANCE), any(Instant.class));
    }

    @Test
    void markStaleSensorsOffline_whenRecentSensorsExist_shouldNotMarkThemOffline() {
        // Recent sensors won't be returned by the query (lastSeen is after cutoff)
        when(sensorRepository.findByActiveTrueAndSensorStatusAndLastSeenBefore(
                eq(SensorStatus.ONLINE), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        sensorStatusScheduler.markStaleSensorsOffline();

        verify(sensorRepository, never()).save(any(Sensor.class));
    }
}
