package com.airproject.airproject.controller;

import com.airproject.airproject.dto.CreateSensorRequest;
import com.airproject.airproject.dto.SensorResponse;
import com.airproject.airproject.dto.UpdateSensorRequest;
import com.airproject.airproject.model.SensorStatus;
import com.airproject.airproject.service.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorControllerTest {

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private SensorController sensorController;

    private UserDetails adminUserDetails;
    private SensorResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminUserDetails = new User("admin@example.com", "password", Collections.emptyList());

        sampleResponse = SensorResponse.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .name("Sensor Patio Central")
                .sensorType("ESP32_AIR")
                .latitude(-12.046374)
                .longitude(-77.042793)
                .firmwareVersion("1.0.2")
                .sensorStatus(SensorStatus.ONLINE)
                .lastSeen(Instant.now())
                .userId(1L)
                .active(true)
                .build();
    }

    @Test
    void getAllSensors_withIncludeInactiveFalse_shouldReturnActiveSensors() {
        when(sensorService.getAllSensors(false)).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<SensorResponse>> response = sensorController.getAllSensors(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ESP32_001", response.getBody().get(0).getUidSensor());
        verify(sensorService).getAllSensors(false);
    }

    @Test
    void getAllSensors_withIncludeInactiveTrue_shouldReturnAllSensors() {
        SensorResponse inactiveResponse = SensorResponse.builder()
                .id(2)
                .uidSensor("ESP32_002")
                .name("Sensor Inactive")
                .active(false)
                .build();

        when(sensorService.getAllSensors(true)).thenReturn(List.of(sampleResponse, inactiveResponse));

        ResponseEntity<List<SensorResponse>> response = sensorController.getAllSensors(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(sensorService).getAllSensors(true);
    }

    @Test
    void getSensorById_shouldReturnSensor() {
        when(sensorService.getSensorById(1)).thenReturn(sampleResponse);

        ResponseEntity<SensorResponse> response = sensorController.getSensorById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void createSensor_shouldReturnCreated() {
        CreateSensorRequest request = CreateSensorRequest.builder()
                .uidSensor("ESP32_001")
                .name("Sensor Patio Central")
                .latitude(-12.046374)
                .longitude(-77.042793)
                .userId(1L)
                .build();

        when(sensorService.createSensor(eq(request), eq("admin@example.com"))).thenReturn(sampleResponse);

        ResponseEntity<SensorResponse> response = sensorController.createSensor(request, adminUserDetails);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void updateSensor_shouldReturnUpdated() {
        UpdateSensorRequest request = UpdateSensorRequest.builder()
                .name("Sensor Renamed")
                .build();

        when(sensorService.updateSensor(eq(1), eq(request))).thenReturn(sampleResponse);

        ResponseEntity<SensorResponse> response = sensorController.updateSensor(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteSensor_shouldReturnNoContent() {
        doNothing().when(sensorService).deleteSensor(1);

        ResponseEntity<Void> response = sensorController.deleteSensor(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(sensorService).deleteSensor(1);
    }

    @Test
    void updateSensor_shouldSetStatusToMaintenance() {
        UpdateSensorRequest request = UpdateSensorRequest.builder()
                .sensorStatus(SensorStatus.MAINTENANCE)
                .build();

        SensorResponse maintenanceResponse = SensorResponse.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .sensorStatus(SensorStatus.MAINTENANCE)
                .active(true)
                .build();

        when(sensorService.updateSensor(eq(1), eq(request))).thenReturn(maintenanceResponse);

        ResponseEntity<SensorResponse> response = sensorController.updateSensor(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SensorStatus.MAINTENANCE, response.getBody().getSensorStatus());
    }

    @Test
    void updateSensor_shouldSetStatusToOffline() {
        UpdateSensorRequest request = UpdateSensorRequest.builder()
                .sensorStatus(SensorStatus.OFFLINE)
                .build();

        SensorResponse offlineResponse = SensorResponse.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .sensorStatus(SensorStatus.OFFLINE)
                .active(true)
                .build();

        when(sensorService.updateSensor(eq(1), eq(request))).thenReturn(offlineResponse);

        ResponseEntity<SensorResponse> response = sensorController.updateSensor(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SensorStatus.OFFLINE, response.getBody().getSensorStatus());
    }

    @Test
    void updateSensor_shouldTransitionFromMaintenanceToOnline() {
        UpdateSensorRequest request = UpdateSensorRequest.builder()
                .sensorStatus(SensorStatus.ONLINE)
                .build();

        SensorResponse onlineResponse = SensorResponse.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .sensorStatus(SensorStatus.ONLINE)
                .active(true)
                .build();

        when(sensorService.updateSensor(eq(1), eq(request))).thenReturn(onlineResponse);

        ResponseEntity<SensorResponse> response = sensorController.updateSensor(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SensorStatus.ONLINE, response.getBody().getSensorStatus());
    }

    @Test
    void reactivateSensor_shouldReturnReactivatedSensor() {
        SensorResponse reactivatedResponse = SensorResponse.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .name("Sensor Patio Central")
                .sensorStatus(SensorStatus.OFFLINE)
                .active(true)
                .build();

        when(sensorService.reactivateSensor(1)).thenReturn(reactivatedResponse);

        ResponseEntity<SensorResponse> response = sensorController.reactivateSensor(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getActive());
        assertEquals(SensorStatus.OFFLINE, response.getBody().getSensorStatus());
    }

    @Test
    void reactivateSensor_whenNotFound_shouldThrow() {
        when(sensorService.reactivateSensor(99))
                .thenThrow(new com.airproject.airproject.exception.SensorNotFoundException("Sensor not found with id: 99"));

        assertThrows(com.airproject.airproject.exception.SensorNotFoundException.class,
                () -> sensorController.reactivateSensor(99));
    }
}
