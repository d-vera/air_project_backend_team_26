package com.airproject.airproject.controller;

import com.airproject.airproject.dto.CreateSensorRequest;
import com.airproject.airproject.dto.SensorResponse;
import com.airproject.airproject.dto.UpdateSensorRequest;
import com.airproject.airproject.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "Sensor Management", description = "Endpoints for managing physical air quality sensors, Google Maps coordinates, and device health status")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    @Operation(summary = "List all active sensors", description = "Retrieves all active sensors with coordinates and status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensors retrieved successfully")
    })
    public ResponseEntity<List<SensorResponse>> getAllSensors() {
        List<SensorResponse> sensors = sensorService.getAllActiveSensors();
        return ResponseEntity.ok(sensors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sensor by ID", description = "Retrieves details of a specific active sensor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<SensorResponse> getSensorById(@PathVariable Integer id) {
        SensorResponse sensor = sensorService.getSensorById(id);
        return ResponseEntity.ok(sensor);
    }

    @PostMapping
    @Operation(summary = "Register a new sensor", description = "Admin endpoint to register a new sensor station.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sensor registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request fields or user not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "409", description = "Sensor with this UID already exists")
    })
    public ResponseEntity<SensorResponse> createSensor(
            @Valid @RequestBody CreateSensorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String authEmail = userDetails != null ? userDetails.getUsername() : null;
        SensorResponse created = sensorService.createSensor(request, authEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sensor", description = "Admin endpoint to update sensor details, coordinates, or assigned user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request fields or user not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<SensorResponse> updateSensor(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSensorRequest request) {
        SensorResponse updated = sensorService.updateSensor(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete sensor", description = "Admin endpoint to soft-delete/deactivate a sensor.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sensor deactivated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<Void> deleteSensor(@PathVariable Integer id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }
}
