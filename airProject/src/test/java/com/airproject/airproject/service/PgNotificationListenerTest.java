package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityReadingNotificationDto;
import com.airproject.airproject.dto.SensorStatusNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgNotificationListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ObjectMapper objectMapper;
    private PgNotificationListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        listener = new PgNotificationListener(messagingTemplate, objectMapper);
    }

    @Test
    void processNotification_AirQualityChannel_ShouldBroadcastToGlobalAndSensorTopics() {
        String payload = """
                {
                    "action": "INSERT",
                    "id": 101,
                    "sensorUid": "ESP32-NODE-01",
                    "deviceName": "Living Room",
                    "timestamp": "2026-10-05T19:00:00Z",
                    "temperature": 22.5,
                    "humidity": 45.0,
                    "co2": 412.0,
                    "pm10Small": 5.0,
                    "pm25": 10.2,
                    "pm10": 15.8
                }
                """;

        listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, payload);

        ArgumentCaptor<AirQualityReadingNotificationDto> captorGlobal =
                ArgumentCaptor.forClass(AirQualityReadingNotificationDto.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/readings"), captorGlobal.capture());

        AirQualityReadingNotificationDto globalDto = captorGlobal.getValue();
        assertEquals("INSERT", globalDto.getAction());
        assertEquals(101L, globalDto.getId());
        assertEquals("ESP32-NODE-01", globalDto.getSensorUid());
        assertEquals(22.5, globalDto.getTemperature());
        assertEquals(412.0, globalDto.getCo2());

        ArgumentCaptor<AirQualityReadingNotificationDto> captorSensor =
                ArgumentCaptor.forClass(AirQualityReadingNotificationDto.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/readings/ESP32-NODE-01"), captorSensor.capture());
        assertEquals("ESP32-NODE-01", captorSensor.getValue().getSensorUid());
    }

    @Test
    void processNotification_AirQualityChannel_WithoutSensorUid_ShouldBroadcastOnlyToGlobal() {
        String payload = """
                {
                    "action": "UPDATE",
                    "id": 102,
                    "temperature": 25.0
                }
                """;

        listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, payload);

        verify(messagingTemplate).convertAndSend(eq("/topic/readings"), any(AirQualityReadingNotificationDto.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void processNotification_SensorStatusChannel_ShouldBroadcastToSensorStatusTopic() {
        String payload = """
                {
                    "action": "UPDATE",
                    "sensorId": 5,
                    "uidSensor": "SENSOR-ABC",
                    "name": "Terrace Sensor",
                    "previousStatus": "ONLINE",
                    "newStatus": "OFFLINE",
                    "active": true,
                    "lastSeen": "2026-10-05T18:30:00Z"
                }
                """;

        listener.processNotification(PgNotificationListener.SENSOR_STATUS_CHANNEL, payload);

        ArgumentCaptor<SensorStatusNotificationDto> captor =
                ArgumentCaptor.forClass(SensorStatusNotificationDto.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/sensors/status"), captor.capture());

        SensorStatusNotificationDto dto = captor.getValue();
        assertEquals(5, dto.getSensorId());
        assertEquals("SENSOR-ABC", dto.getUidSensor());
        assertEquals("Terrace Sensor", dto.getName());
        assertEquals("ONLINE", dto.getPreviousStatus());
        assertEquals("OFFLINE", dto.getNewStatus());
        assertTrue(dto.getActive());
    }

    @Test
    void processNotification_WithNullOrBlankPayload_ShouldDoNothing() {
        listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, null);
        listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, "");
        listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, "   ");

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void processNotification_WithMalformedJson_ShouldCatchExceptionAndNotThrow() {
        assertDoesNotThrow(() ->
                listener.processNotification(PgNotificationListener.AIR_QUALITY_CHANNEL, "{invalid-json")
        );
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void processNotification_WithUnknownChannel_ShouldDoNothing() {
        listener.processNotification("unknown_channel", "{\"some\":\"data\"}");
        verifyNoInteractions(messagingTemplate);
    }
}
