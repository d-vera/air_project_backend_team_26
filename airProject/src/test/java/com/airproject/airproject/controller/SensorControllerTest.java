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
    void getAllSensors_shouldReturnList() {
        when(sensorService.getAllActiveSensors()).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<SensorResponse>> response = sensorController.getAllSensors();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ESP32_001", response.getBody().get(0).getUidSensor());
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
}
