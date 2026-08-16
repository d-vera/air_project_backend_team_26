package com.airproject.airproject.service;

import com.airproject.airproject.dto.CreateSensorRequest;
import com.airproject.airproject.dto.SensorResponse;
import com.airproject.airproject.dto.UpdateSensorRequest;
import com.airproject.airproject.exception.SensorAlreadyExistsException;
import com.airproject.airproject.exception.SensorNotFoundException;
import com.airproject.airproject.exception.UserNotFoundException;
import com.airproject.airproject.model.Sensor;
import com.airproject.airproject.model.SensorStatus;
import com.airproject.airproject.model.User;
import com.airproject.airproject.repository.SensorRepository;
import com.airproject.airproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensorService {

    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);

    private final SensorRepository sensorRepository;
    private final UserRepository userRepository;

    public SensorService(SensorRepository sensorRepository, UserRepository userRepository) {
        this.sensorRepository = sensorRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> getAllActiveSensors() {
        return sensorRepository.findByActiveTrue().stream()
                .map(SensorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SensorResponse getSensorById(Integer id) {
        Sensor sensor = sensorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SensorNotFoundException("Sensor not found with id: " + id));
        return SensorResponse.fromEntity(sensor);
    }

    @Transactional
    public SensorResponse createSensor(CreateSensorRequest request, String authenticatedEmail) {
        if (sensorRepository.existsByUidSensor(request.getUidSensor())) {
            throw new SensorAlreadyExistsException("Sensor with UID '" + request.getUidSensor() + "' is already registered");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findByIdAndActiveTrue(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
        } else if (StringUtils.hasText(authenticatedEmail)) {
            user = userRepository.findByEmailAndActiveTrue(authenticatedEmail).orElse(null);
        }

        Sensor sensor = Sensor.builder()
                .uidSensor(request.getUidSensor())
                .name(request.getName())
                .sensorType(StringUtils.hasText(request.getSensorType()) ? request.getSensorType() : "ESP32_AIR")
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .firmwareVersion(request.getFirmwareVersion())
                .sensorStatus(SensorStatus.OFFLINE)
                .user(user)
                .active(true)
                .build();

        Sensor savedSensor = sensorRepository.save(sensor);
        logger.info("Successfully registered new sensor: id={}, uid={}", savedSensor.getId(), savedSensor.getUidSensor());
        return SensorResponse.fromEntity(savedSensor);
    }

    @Transactional
    public SensorResponse updateSensor(Integer id, UpdateSensorRequest request) {
        Sensor sensor = sensorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SensorNotFoundException("Sensor not found with id: " + id));

        if (StringUtils.hasText(request.getName())) {
            sensor.setName(request.getName());
        }
        if (StringUtils.hasText(request.getSensorType())) {
            sensor.setSensorType(request.getSensorType());
        }
        if (request.getLatitude() != null) {
            sensor.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            sensor.setLongitude(request.getLongitude());
        }
        if (StringUtils.hasText(request.getFirmwareVersion())) {
            sensor.setFirmwareVersion(request.getFirmwareVersion());
        }
        if (request.getSensorStatus() != null) {
            sensor.setSensorStatus(request.getSensorStatus());
        }
        if (request.getUserId() != null) {
            User user = userRepository.findByIdAndActiveTrue(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
            sensor.setUser(user);
        }

        Sensor updated = sensorRepository.save(sensor);
        logger.info("Successfully updated sensor: id={}, uid={}", updated.getId(), updated.getUidSensor());
        return SensorResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSensor(Integer id) {
        Sensor sensor = sensorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SensorNotFoundException("Sensor not found with id: " + id));

        sensor.setActive(false);
        sensorRepository.save(sensor);
        logger.info("Soft-deleted sensor: id={}", id);
    }

    @Transactional
    public void updateSensorStatusFromReading(String uidSensor, String firmwareVersion, Instant readingTime) {
        if (!StringUtils.hasText(uidSensor)) {
            return;
        }

        sensorRepository.findByUidSensor(uidSensor).ifPresentOrElse(
                sensor -> {
                    sensor.setLastSeen(readingTime != null ? readingTime : Instant.now());
                    sensor.setSensorStatus(SensorStatus.ONLINE);
                    if (StringUtils.hasText(firmwareVersion)) {
                        sensor.setFirmwareVersion(firmwareVersion);
                    }
                    sensorRepository.save(sensor);
                    logger.debug("Updated sensor status to ONLINE for UID: {}", uidSensor);
                },
                () -> logger.warn("Received reading from unregistered sensor UID: {}", uidSensor)
        );
    }
}
