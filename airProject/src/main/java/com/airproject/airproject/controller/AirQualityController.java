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
import java.util.Map;

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
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters (e.g., range and from/to both provided, from after to)"),
            @ApiResponse(responseCode = "403", description = "Visitors cannot access LAST_YEAR or custom ranges")
    })
    public ResponseEntity<?> getHistoricalData(
            @Parameter(description = "Predefined time range: LAST_DAY, LAST_WEEK, LAST_MONTH, LAST_YEAR")
            @RequestParam(required = false) TimeRange range,

            @Parameter(description = "Custom range start (ISO 8601). Requires authentication. Mutually exclusive with 'range'.")
            @RequestParam(required = false) Instant from,

            @Parameter(description = "Custom range end (ISO 8601). Requires authentication. Mutually exclusive with 'range'.")
            @RequestParam(required = false) Instant to,

            @Parameter(description = "Filter by device ID. Optional — omit to get all devices.")
            @RequestParam(required = false) String deviceId
    ) {
        try {
            HistoricalDataResponse response = airQualityService.getHistoricalData(range, from, to, deviceId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
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
}
