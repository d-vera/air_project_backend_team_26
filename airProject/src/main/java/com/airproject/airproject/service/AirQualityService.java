package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityMessage;
import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.repository.AirQualityReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityService.class);

    private final AirQualityReadingRepository repository;

    public AirQualityService(AirQualityReadingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AirQualityReading processAndSave(AirQualityMessage message, String topic) {
        String deviceId = message.dispositivo() != null ? message.dispositivo().id() : "unknown";
        String firmware = message.dispositivo() != null ? message.dispositivo().firmware() : null;
        Integer sequence = message.dispositivo() != null ? message.dispositivo().secuencia() : null;

        Double temperature = message.entorno() != null ? message.entorno().temperatura() : null;
        Double humidity = message.entorno() != null ? message.entorno().humedad() : null;

        Double co2 = message.aire() != null ? message.aire().co2() : null;
        Double pm10Small = message.aire() != null ? message.aire().pm1_0() : null;
        Double pm25 = message.aire() != null ? message.aire().pm2_5() : null;
        Double pm10 = message.aire() != null ? message.aire().pm10() : null;

        AirQualityReading reading = new AirQualityReading(
                Instant.now(),
                deviceId,
                firmware,
                sequence,
                topic,
                temperature,
                humidity,
                co2,
                pm10Small,
                pm25,
                pm10
        );

        AirQualityReading savedReading = repository.save(reading);
        logger.info("Successfully ingested air quality reading from device: {} on topic: {}", deviceId, topic);
        return savedReading;
    }
}
