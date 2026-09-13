package com.airproject.airproject.controller;

import com.airproject.airproject.dto.CurrentReadingResponse;
import com.airproject.airproject.dto.HistoricalDataResponse;
import com.airproject.airproject.dto.TimeRange;
import com.airproject.airproject.service.AirQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/air-quality")
@Tag(name = "Air Quality", description = "Endpoints for querying air quality data (historical and current)")
public class AirQualityController {

    private final AirQualityService airQualityService;

    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    @GetMapping("/historical")
    @Operation(
            summary = "Get historical air quality data",
            description = """
                    Returns time-aggregated average air quality data. Use either a predefined 'range' 
                    (LAST_DAY, LAST_WEEK, LAST_MONTH, LAST_YEAR) or a custom date range ('from'/'to'). 
                    Visitors can only use LAST_DAY, LAST_WEEK, and LAST_MONTH. 
                    Authenticated users can also use LAST_YEAR and custom ranges.
                    Aggregation interval is automatically selected based on range duration.
                    Date parameters accept ISO-8601 Instant (e.g., 2026-09-29T00:00:00Z) or date-only 
                    (e.g., 2026-09-29) formats.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters (e.g., range and from/to both provided, from after to, invalid date format)"),
            @ApiResponse(responseCode = "403", description = "Visitors cannot access LAST_YEAR or custom ranges")
    })
    public ResponseEntity<?> getHistoricalData(
            @Parameter(description = "Predefined time range: LAST_DAY, LAST_WEEK, LAST_MONTH, LAST_YEAR")
            @RequestParam(required = false) TimeRange range,

            @Parameter(description = "Custom range start (ISO-8601 Instant or YYYY-MM-DD). Requires authentication. Mutually exclusive with 'range'.")
            @RequestParam(required = false) String from,

            @Parameter(description = "Custom range end (ISO-8601 Instant or YYYY-MM-DD). Requires authentication. Mutually exclusive with 'range'.")
            @RequestParam(required = false) String to,

            @Parameter(description = "Filter by device ID. Optional — omit to get all devices.")
            @RequestParam(required = false) String deviceId
    ) {
        Instant fromInstant = parseFlexibleInstant(from, "from", true);
        Instant toInstant = parseFlexibleInstant(to, "to", false);

        HistoricalDataResponse response = airQualityService.getHistoricalData(range, fromInstant, toInstant, deviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current air quality readings",
            description = "Returns the latest air quality reading for each device. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current readings retrieved successfully")
    })
    public ResponseEntity<CurrentReadingResponse> getCurrentReadings(
            @Parameter(description = "Filter by device ID. Optional — omit to get all devices.")
            @RequestParam(required = false) String deviceId
    ) {
        CurrentReadingResponse response = airQualityService.getCurrentReadings(deviceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Parses a date string flexibly, accepting either:
     * <ul>
     *   <li>ISO-8601 Instant format: {@code 2026-09-29T00:00:00Z}</li>
     *   <li>Date-only format: {@code 2026-09-29} → start-of-day (00:00:00Z) for 'from',
     *       end-of-day (23:59:59Z) for 'to'</li>
     * </ul>
     *
     * @param value     the raw string value from the query parameter (may be null)
     * @param paramName the parameter name (for error messages)
     * @param isFrom    true if this is the 'from' parameter (start of day), false for 'to' (end of day)
     * @return the parsed Instant, or null if value is null/blank
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    static Instant parseFlexibleInstant(String value, String paramName, boolean isFrom) {
        if (value == null || value.isBlank()) {
            return null;
        }

        // Try full ISO-8601 Instant first (e.g., 2026-09-29T00:00:00Z)
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // Fall through to try date-only format
        }

        // Try date-only format (YYYY-MM-DD)
        try {
            LocalDate date = LocalDate.parse(value);
            if (isFrom) {
                return date.atStartOfDay(ZoneOffset.UTC).toInstant();
            } else {
                return date.atTime(LocalTime.of(23, 59, 59)).toInstant(ZoneOffset.UTC);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for parameter '%s'. Expected ISO-8601 UTC format (e.g., 2026-09-29T00:00:00Z) or date-only format (e.g., 2026-09-29).",
                    value, paramName));
        }
    }
}

