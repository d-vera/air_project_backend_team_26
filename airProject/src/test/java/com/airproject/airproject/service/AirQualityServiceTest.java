package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityMessage;
import com.airproject.airproject.dto.CurrentReadingResponse;
import com.airproject.airproject.dto.HistoricalDataResponse;
import com.airproject.airproject.dto.TimeRange;
import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.repository.AirQualityReadingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirQualityServiceTest {

    @Mock
    private AirQualityReadingRepository repository;

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private AirQualityService airQualityService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldProcessAndSaveMessage() {
        AirQualityMessage.Device device = new AirQualityMessage.Device(
                "ACEA5AC8E720",
                "3.2.3",
                1791835722L
        );
        AirQualityMessage.Environment environment = new AirQualityMessage.Environment(30.99222, 22.49485);
        AirQualityMessage.AirData airData = new AirQualityMessage.AirData(687.0, 0.0, 0.0, 1.0);
        AirQualityMessage message = new AirQualityMessage(device, environment, airData);

        when(repository.save(any(AirQualityReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AirQualityReading result = airQualityService.processAndSave(message, "calidad_aire/nodo1");

        assertNotNull(result);
        assertEquals("ACEA5AC8E720", result.getDeviceId());
        assertNull(result.getDeviceName());
        assertEquals("3.2.3", result.getFirmware());
        assertNull(result.getSequence());
        assertEquals("calidad_aire/nodo1", result.getTopic());
        assertEquals(Instant.ofEpochSecond(1791835722L), result.getTime());
        assertEquals(30.99222, result.getTemperature());
        assertEquals(22.49485, result.getHumidity());
        assertEquals(687.0, result.getCo2());
        assertEquals(0.0, result.getPm1_0());
        assertEquals(0.0, result.getPm2_5());
        assertEquals(1.0, result.getPm10());

        ArgumentCaptor<AirQualityReading> captor = ArgumentCaptor.forClass(AirQualityReading.class);
        verify(repository).save(captor.capture());
        AirQualityReading saved = captor.getValue();
        assertNull(saved.getDeviceName());
        assertNull(saved.getSequence());
        assertEquals(Instant.ofEpochSecond(1791835722L), saved.getTime());

        verify(sensorService).updateSensorStatusFromReading("ACEA5AC8E720", "3.2.3", Instant.ofEpochSecond(1791835722L));
    }

    @Test
    void shouldFallbackToCurrentTimeWhenTimestampIsNull() {
        AirQualityMessage.Device device = new AirQualityMessage.Device(
                "ACEA5AC8E720",
                "3.2.3",
                null
        );
        AirQualityMessage message = new AirQualityMessage(device, null, null);

        when(repository.save(any(AirQualityReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now().minusSeconds(1);
        AirQualityReading result = airQualityService.processAndSave(message, "calidad_aire/nodo1");
        Instant after = Instant.now().plusSeconds(1);

        assertNotNull(result.getTime());
        assertTrue(result.getTime().isAfter(before) && result.getTime().isBefore(after));
    }

    // ==================== Interval Calculation Tests ====================

    @Test
    void calculateInterval_shouldReturn10MinutesForRangeUpTo1Day() {
        Instant now = Instant.now();
        assertEquals("10 minutes", airQualityService.calculateInterval(now.minus(6, ChronoUnit.HOURS), now));
        assertEquals("10 minutes", airQualityService.calculateInterval(now.minus(1, ChronoUnit.DAYS), now));
    }

    @Test
    void calculateInterval_shouldReturn1HourForRangeUpTo1Week() {
        Instant now = Instant.now();
        assertEquals("1 hour", airQualityService.calculateInterval(now.minus(3, ChronoUnit.DAYS), now));
        assertEquals("1 hour", airQualityService.calculateInterval(now.minus(7, ChronoUnit.DAYS), now));
    }

    @Test
    void calculateInterval_shouldReturn12HoursForRangeUpTo1Month() {
        Instant now = Instant.now();
        assertEquals("12 hours", airQualityService.calculateInterval(now.minus(15, ChronoUnit.DAYS), now));
        assertEquals("12 hours", airQualityService.calculateInterval(now.minus(31, ChronoUnit.DAYS), now));
    }

    @Test
    void calculateInterval_shouldReturn24HoursForRangeOver1Month() {
        Instant now = Instant.now();
        assertEquals("24 hours", airQualityService.calculateInterval(now.minus(60, ChronoUnit.DAYS), now));
        assertEquals("24 hours", airQualityService.calculateInterval(now.minus(365, ChronoUnit.DAYS), now));
        assertEquals("24 hours", airQualityService.calculateInterval(now.minus(730, ChronoUnit.DAYS), now));
    }

    // ==================== Range Resolution Tests ====================

    @Test
    void resolveTimeRange_shouldResolveLastDayTo24HoursAgo() {
        Instant before = Instant.now().minus(1, ChronoUnit.DAYS).minusSeconds(1);
        Instant[] result = airQualityService.resolveTimeRange(TimeRange.LAST_DAY);
        assertTrue(result[0].isAfter(before));
        assertTrue(result[1].isAfter(result[0]));
    }

    @Test
    void resolveTimeRange_shouldResolveLastWeekTo7DaysAgo() {
        Instant before = Instant.now().minus(7, ChronoUnit.DAYS).minusSeconds(2);
        Instant[] result = airQualityService.resolveTimeRange(TimeRange.LAST_WEEK);
        assertTrue(result[0].isAfter(before));
    }

    @Test
    void resolveTimeRange_shouldResolveLastMonthTo31DaysAgo() {
        Instant before = Instant.now().minus(31, ChronoUnit.DAYS).minusSeconds(2);
        Instant[] result = airQualityService.resolveTimeRange(TimeRange.LAST_MONTH);
        assertTrue(result[0].isAfter(before));
    }

    @Test
    void resolveTimeRange_shouldResolveLastYearTo365DaysAgo() {
        Instant before = Instant.now().minus(365, ChronoUnit.DAYS).minusSeconds(2);
        Instant[] result = airQualityService.resolveTimeRange(TimeRange.LAST_YEAR);
        assertTrue(result[0].isAfter(before));
    }

    // ==================== Access Control Tests ====================

    @Test
    void getHistoricalData_visitorCanAccessLastDay() {
        // No authentication set → anonymous
        when(repository.findAggregatedReadings(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        HistoricalDataResponse response = airQualityService.getHistoricalData(
                TimeRange.LAST_DAY, null, null, null);

        assertNotNull(response);
        assertEquals("10 minutes", response.getAggregationInterval());
    }

    @Test
    void getHistoricalData_visitorCanAccessLastWeek() {
        when(repository.findAggregatedReadings(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        HistoricalDataResponse response = airQualityService.getHistoricalData(
                TimeRange.LAST_WEEK, null, null, null);

        assertNotNull(response);
        assertEquals("1 hour", response.getAggregationInterval());
    }

    @Test
    void getHistoricalData_visitorCanAccessLastMonth() {
        when(repository.findAggregatedReadings(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        HistoricalDataResponse response = airQualityService.getHistoricalData(
                TimeRange.LAST_MONTH, null, null, null);

        assertNotNull(response);
        assertEquals("12 hours", response.getAggregationInterval());
    }

    @Test
    void getHistoricalData_visitorCannotAccessLastYear() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                airQualityService.getHistoricalData(TimeRange.LAST_YEAR, null, null, null));
    }

    @Test
    void getHistoricalData_visitorCannotUseCustomRange() {
        Instant from = Instant.now().minus(90, ChronoUnit.DAYS);
        Instant to = Instant.now();

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                airQualityService.getHistoricalData(null, from, to, null));
    }

    @Test
    void getHistoricalData_authenticatedUserCanAccessLastYear() {
        setUpAuthenticatedUser();
        when(repository.findAggregatedReadings(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        HistoricalDataResponse response = airQualityService.getHistoricalData(
                TimeRange.LAST_YEAR, null, null, null);

        assertNotNull(response);
        assertEquals("24 hours", response.getAggregationInterval());
    }

    @Test
    void getHistoricalData_authenticatedUserCanUseCustomRange() {
        setUpAuthenticatedUser();
        Instant from = Instant.now().minus(90, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(repository.findAggregatedReadings(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        HistoricalDataResponse response = airQualityService.getHistoricalData(null, from, to, null);

        assertNotNull(response);
        assertEquals("24 hours", response.getAggregationInterval());
    }

    // ==================== Validation Tests ====================

    @Test
    void getHistoricalData_shouldRejectMutuallyExclusiveParams() {
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        assertThrows(IllegalArgumentException.class, () ->
                airQualityService.getHistoricalData(TimeRange.LAST_DAY, from, to, null));
    }

    @Test
    void getHistoricalData_shouldRejectFromAfterTo() {
        Instant from = Instant.now();
        Instant to = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThrows(IllegalArgumentException.class, () ->
                airQualityService.getHistoricalData(null, from, to, null));
    }

    @Test
    void getHistoricalData_shouldRejectMissingRangeAndDates() {
        assertThrows(IllegalArgumentException.class, () ->
                airQualityService.getHistoricalData(null, null, null, null));
    }

    // ==================== Current Readings Tests ====================

    @Test
    void getCurrentReadings_shouldReturnEmptyListWhenNoData() {
        when(repository.findLatestReadingPerDevice(null)).thenReturn(Collections.emptyList());

        CurrentReadingResponse response = airQualityService.getCurrentReadings(null);

        assertNotNull(response);
        assertTrue(response.getReadings().isEmpty());
    }

    @Test
    void getCurrentReadings_shouldReturnLatestReadingsPerDevice() {
        AirQualityReading reading1 = new AirQualityReading(
                Instant.now(), "ESP32_001", "Node1", "1.0", 1, "topic",
                25.0, 60.0, 400.0, 8.0, 15.0, 22.0);
        AirQualityReading reading2 = new AirQualityReading(
                Instant.now(), "ESP32_002", "Node2", "1.0", 2, "topic",
                26.0, 58.0, 420.0, 9.0, 16.0, 24.0);

        when(repository.findLatestReadingPerDevice(null)).thenReturn(List.of(reading1, reading2));

        CurrentReadingResponse response = airQualityService.getCurrentReadings(null);

        assertNotNull(response);
        assertEquals(2, response.getReadings().size());
        assertEquals("ESP32_001", response.getReadings().get(0).getDeviceId());
        assertEquals("ESP32_002", response.getReadings().get(1).getDeviceId());
    }

    @Test
    void getCurrentReadings_shouldFilterByDeviceId() {
        AirQualityReading reading = new AirQualityReading(
                Instant.now(), "ESP32_001", "Node1", "1.0", 1, "topic",
                25.0, 60.0, 400.0, 8.0, 15.0, 22.0);

        when(repository.findLatestReadingPerDevice("ESP32_001")).thenReturn(List.of(reading));

        CurrentReadingResponse response = airQualityService.getCurrentReadings("ESP32_001");

        assertNotNull(response);
        assertEquals(1, response.getReadings().size());
        assertEquals("ESP32_001", response.getReadings().get(0).getDeviceId());
        verify(repository).findLatestReadingPerDevice("ESP32_001");
    }

    // ==================== Helper Methods ====================

    private void setUpAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}

