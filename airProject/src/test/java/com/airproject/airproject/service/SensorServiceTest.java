package com.airproject.airproject.service;

import com.airproject.airproject.dto.CreateSensorRequest;
import com.airproject.airproject.dto.SensorResponse;
import com.airproject.airproject.dto.UpdateSensorRequest;
import com.airproject.airproject.exception.SensorAlreadyExistsException;
import com.airproject.airproject.exception.SensorNotFoundException;
import com.airproject.airproject.exception.UserNotFoundException;
import com.airproject.airproject.model.Role;
import com.airproject.airproject.model.Sensor;
import com.airproject.airproject.model.SensorStatus;
import com.airproject.airproject.model.User;
import com.airproject.airproject.repository.SensorRepository;
import com.airproject.airproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SensorService sensorService;

    private User sampleUser;
    private Sensor sampleSensor;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .active(true)
                .build();

        sampleSensor = Sensor.builder()
                .id(1)
                .uidSensor("ESP32_001")
                .name("Sensor Patio Central")
                .sensorType("ESP32_AIR")
                .latitude(-12.046374)
                .longitude(-77.042793)
                .firmwareVersion("1.0.2")
                .sensorStatus(SensorStatus.ONLINE)
                .lastSeen(Instant.now())
                .user(sampleUser)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getAllActiveSensors_shouldReturnListOfSensors() {
        when(sensorRepository.findByActiveTrue()).thenReturn(List.of(sampleSensor));

        List<SensorResponse> result = sensorService.getAllActiveSensors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ESP32_001", result.get(0).getUidSensor());
        assertEquals("Sensor Patio Central", result.get(0).getName());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void getSensorById_whenSensorExists_shouldReturnSensor() {
        when(sensorRepository.findByIdAndActiveTrue(1)).thenReturn(Optional.of(sampleSensor));

        SensorResponse result = sensorService.getSensorById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("ESP32_001", result.getUidSensor());
    }

    @Test
    void getSensorById_whenSensorDoesNotExist_shouldThrowNotFoundException() {
        when(sensorRepository.findByIdAndActiveTrue(99)).thenReturn(Optional.empty());

        assertThrows(SensorNotFoundException.class, () -> sensorService.getSensorById(99));
    }

    @Test
    void createSensor_whenValid_shouldSaveAndReturnSensor() {
        CreateSensorRequest request = CreateSensorRequest.builder()
                .uidSensor("ESP32_002")
                .name("Sensor Norte")
                .sensorType("ESP32_AIR")
                .latitude(-12.05)
                .longitude(-77.05)
                .userId(1L)
                .build();

        when(sensorRepository.existsByUidSensor("ESP32_002")).thenReturn(false);
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleUser));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(invocation -> {
            Sensor s = invocation.getArgument(0);
            s.setId(2);
            return s;
        });

        SensorResponse response = sensorService.createSensor(request, "admin@example.com");

        assertNotNull(response);
        assertEquals(2, response.getId());
        assertEquals("ESP32_002", response.getUidSensor());
        assertEquals("Sensor Norte", response.getName());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void createSensor_whenUidAlreadyExists_shouldThrowAlreadyExistsException() {
        CreateSensorRequest request = CreateSensorRequest.builder()
                .uidSensor("ESP32_001")
                .name("Sensor Duplicado")
                .latitude(-12.0)
                .longitude(-77.0)
                .build();

        when(sensorRepository.existsByUidSensor("ESP32_001")).thenReturn(true);

        assertThrows(SensorAlreadyExistsException.class, () -> sensorService.createSensor(request, "admin@example.com"));
    }

    @Test
    void createSensor_whenUserIdNotFound_shouldThrowUserNotFoundException() {
        CreateSensorRequest request = CreateSensorRequest.builder()
                .uidSensor("ESP32_003")
                .name("Sensor Test")
                .latitude(-12.0)
                .longitude(-77.0)
                .userId(99L)
                .build();

        when(sensorRepository.existsByUidSensor("ESP32_003")).thenReturn(false);
        when(userRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> sensorService.createSensor(request, "admin@example.com"));
    }

    @Test
    void updateSensor_whenValid_shouldUpdateAndReturnSensor() {
        UpdateSensorRequest request = UpdateSensorRequest.builder()
                .name("Sensor Updated Name")
                .latitude(-12.10)
                .longitude(-77.10)
                .sensorStatus(SensorStatus.MAINTENANCE)
                .build();

        when(sensorRepository.findByIdAndActiveTrue(1)).thenReturn(Optional.of(sampleSensor));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorResponse response = sensorService.updateSensor(1, request);

        assertNotNull(response);
        assertEquals("Sensor Updated Name", response.getName());
        assertEquals(-12.10, response.getLatitude());
        assertEquals(-77.10, response.getLongitude());
        assertEquals(SensorStatus.MAINTENANCE, response.getSensorStatus());
    }

    @Test
    void deleteSensor_whenValid_shouldSoftDelete() {
        when(sensorRepository.findByIdAndActiveTrue(1)).thenReturn(Optional.of(sampleSensor));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sensorService.deleteSensor(1);

        assertFalse(sampleSensor.getActive());
        verify(sensorRepository).save(sampleSensor);
    }

    @Test
    void updateSensorStatusFromReading_whenSensorExists_shouldUpdateOnlineStatusAndLastSeen() {
        when(sensorRepository.findByUidSensor("ESP32_001")).thenReturn(Optional.of(sampleSensor));

        Instant readingTime = Instant.now();
        sensorService.updateSensorStatusFromReading("ESP32_001", "1.0.3", readingTime);

        assertEquals(SensorStatus.ONLINE, sampleSensor.getSensorStatus());
        assertEquals("1.0.3", sampleSensor.getFirmwareVersion());
        assertEquals(readingTime, sampleSensor.getLastSeen());
        verify(sensorRepository).save(sampleSensor);
    }
}
