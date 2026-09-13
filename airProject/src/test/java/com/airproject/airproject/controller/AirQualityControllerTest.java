package com.airproject.airproject.controller;

import com.airproject.airproject.dto.HistoricalDataResponse;
import com.airproject.airproject.dto.TimeRange;
import com.airproject.airproject.service.AirQualityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirQualityControllerTest {

    @Mock
    private AirQualityService airQualityService;

    @InjectMocks
    private AirQualityController airQualityController;

    // ==================== Happy-path tests ====================

    @Test
    void getHistoricalData_withValidRange_shouldReturn200() {
        HistoricalDataResponse mockResponse = new HistoricalDataResponse(
                new HistoricalDataResponse.Range(Instant.now().minusSeconds(86400), Instant.now()),
                "10 minutes",
                Collections.emptyList()
        );

        when(airQualityService.getHistoricalData(eq(TimeRange.LAST_DAY), isNull(), isNull(), isNull()))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = airQualityController.getHistoricalData(TimeRange.LAST_DAY, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(HistoricalDataResponse.class, response.getBody());
    }

    @Test
    void getHistoricalData_withIsoInstantDates_shouldReturn200() {
        String fromStr = "2026-09-01T00:00:00Z";
        String toStr = "2026-09-29T23:59:59Z";

        Instant expectedFrom = Instant.parse(fromStr);
        Instant expectedTo = Instant.parse(toStr);

        HistoricalDataResponse mockResponse = new HistoricalDataResponse(
                new HistoricalDataResponse.Range(expectedFrom, expectedTo),
                "24 hours",
                Collections.emptyList()
        );

        when(airQualityService.getHistoricalData(isNull(), eq(expectedFrom), eq(expectedTo), isNull()))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = airQualityController.getHistoricalData(null, fromStr, toStr, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getHistoricalData_withDateOnlyFormat_shouldParseAndReturn200() {
        String fromStr = "2026-09-01";
        String toStr = "2026-09-29";

        Instant expectedFrom = LocalDate.of(2026, 9, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedTo = LocalDate.of(2026, 9, 29).atTime(LocalTime.of(23, 59, 59)).toInstant(ZoneOffset.UTC);

        HistoricalDataResponse mockResponse = new HistoricalDataResponse(
                new HistoricalDataResponse.Range(expectedFrom, expectedTo),
                "24 hours",
                Collections.emptyList()
        );

        when(airQualityService.getHistoricalData(isNull(), eq(expectedFrom), eq(expectedTo), isNull()))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = airQualityController.getHistoricalData(null, fromStr, toStr, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ==================== Flexible parsing unit tests ====================

    @Test
    void parseFlexibleInstant_withNull_shouldReturnNull() {
        assertNull(AirQualityController.parseFlexibleInstant(null, "from", true));
    }

    @Test
    void parseFlexibleInstant_withBlank_shouldReturnNull() {
        assertNull(AirQualityController.parseFlexibleInstant("  ", "from", true));
    }

    @Test
    void parseFlexibleInstant_withIsoInstant_shouldParseLiterally() {
        Instant result = AirQualityController.parseFlexibleInstant("2026-09-29T12:30:00Z", "from", true);
        assertEquals(Instant.parse("2026-09-29T12:30:00Z"), result);
    }

    @Test
    void parseFlexibleInstant_withDateOnly_from_shouldReturnStartOfDay() {
        Instant result = AirQualityController.parseFlexibleInstant("2026-09-29", "from", true);
        assertEquals(Instant.parse("2026-09-29T00:00:00Z"), result);
    }

    @Test
    void parseFlexibleInstant_withDateOnly_to_shouldReturnEndOfDay() {
        Instant result = AirQualityController.parseFlexibleInstant("2026-09-29", "to", false);
        assertEquals(Instant.parse("2026-09-29T23:59:59Z"), result);
    }

    @Test
    void parseFlexibleInstant_withInvalidString_shouldThrowIllegalArgument() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> AirQualityController.parseFlexibleInstant("not-a-date", "from", true)
        );
        assertTrue(ex.getMessage().contains("Invalid value 'not-a-date' for parameter 'from'"));
        assertTrue(ex.getMessage().contains("ISO-8601"));
    }

    @Test
    void parseFlexibleInstant_withPartialDate_shouldThrowIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AirQualityController.parseFlexibleInstant("2026-13-45", "to", false)
        );
    }

    // ==================== Validation error tests ====================

    @Test
    void getHistoricalData_withFromAfterTo_shouldPropagate400ViaService() {
        // When 'from' is after 'to', the service throws IllegalArgumentException,
        // which GlobalExceptionHandler maps to 400.
        when(airQualityService.getHistoricalData(isNull(), any(Instant.class), any(Instant.class), isNull()))
                .thenThrow(new IllegalArgumentException("Parameter 'from' must be before 'to'."));

        assertThrows(
                IllegalArgumentException.class,
                () -> airQualityController.getHistoricalData(null, "2026-09-30T00:00:00Z", "2026-09-01T00:00:00Z", null)
        );
    }

    @Test
    void getHistoricalData_withRangeAndFromTo_shouldPropagate400ViaService() {
        // When both range and from/to are provided, the service throws IllegalArgumentException.
        when(airQualityService.getHistoricalData(eq(TimeRange.LAST_DAY), any(Instant.class), any(Instant.class), isNull()))
                .thenThrow(new IllegalArgumentException("Parameters 'range' and 'from/to' are mutually exclusive. Use one or the other."));

        assertThrows(
                IllegalArgumentException.class,
                () -> airQualityController.getHistoricalData(TimeRange.LAST_DAY, "2026-09-01T00:00:00Z", "2026-09-29T00:00:00Z", null)
        );
    }

    @Test
    void getHistoricalData_withNeitherRangeNorFromTo_shouldPropagate400ViaService() {
        when(airQualityService.getHistoricalData(isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("Either 'range' or both 'from' and 'to' must be provided."));

        assertThrows(
                IllegalArgumentException.class,
                () -> airQualityController.getHistoricalData(null, null, null, null)
        );
    }
}
