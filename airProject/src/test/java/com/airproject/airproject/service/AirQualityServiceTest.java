package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityMessage;
import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.repository.AirQualityReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirQualityServiceTest {

    @Mock
    private AirQualityReadingRepository repository;

    @InjectMocks
    private AirQualityService airQualityService;

    @Test
    void shouldProcessAndSaveMessageWithNombreAndTimestamp() {
        AirQualityMessage.Dispositivo dispositivo = new AirQualityMessage.Dispositivo(
                "ACEA5AC8E720",
                "Node1",
                "1.0.2",
                109,
                1785274877L
        );
        AirQualityMessage.Entorno entorno = new AirQualityMessage.Entorno(23.7, 20.16699);
        AirQualityMessage.Aire aire = new AirQualityMessage.Aire(482.0, 19.29231, 32.15385, 38.58462);
        AirQualityMessage message = new AirQualityMessage(dispositivo, entorno, aire);

        when(repository.save(any(AirQualityReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AirQualityReading result = airQualityService.processAndSave(message, "calidad_aire/nodo1");

        assertNotNull(result);
        assertEquals("ACEA5AC8E720", result.getDeviceId());
        assertEquals("Node1", result.getDeviceName());
        assertEquals("1.0.2", result.getFirmware());
        assertEquals(109, result.getSequence());
        assertEquals("calidad_aire/nodo1", result.getTopic());
        assertEquals(Instant.ofEpochSecond(1785274877L), result.getTime());
        assertEquals(23.7, result.getTemperature());
        assertEquals(20.16699, result.getHumidity());
        assertEquals(482.0, result.getCo2());

        ArgumentCaptor<AirQualityReading> captor = ArgumentCaptor.forClass(AirQualityReading.class);
        verify(repository).save(captor.capture());
        AirQualityReading saved = captor.getValue();
        assertEquals("Node1", saved.getDeviceName());
        assertEquals(Instant.ofEpochSecond(1785274877L), saved.getTime());
    }

    @Test
    void shouldFallbackToCurrentTimeWhenTimestampIsNull() {
        AirQualityMessage.Dispositivo dispositivo = new AirQualityMessage.Dispositivo(
                "ACEA5AC8E720",
                "Node1",
                "1.0.2",
                109,
                null
        );
        AirQualityMessage message = new AirQualityMessage(dispositivo, null, null);

        when(repository.save(any(AirQualityReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now().minusSeconds(1);
        AirQualityReading result = airQualityService.processAndSave(message, "calidad_aire/nodo1");
        Instant after = Instant.now().plusSeconds(1);

        assertNotNull(result.getTime());
        assertTrue(result.getTime().isAfter(before) && result.getTime().isBefore(after));
    }
}
