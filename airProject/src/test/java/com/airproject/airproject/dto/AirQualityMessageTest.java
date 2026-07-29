package com.airproject.airproject.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirQualityMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeUpdatedPayloadWithNombreAndTimestamp() throws Exception {
        String json = """
                {
                  "dispositivo": {
                    "id": "ACEA5AC8E720",
                    "nombre": "Node1",
                    "firmware": "1.0.2",
                    "secuencia": 109,
                    "Timestamp": 1785274877
                  },
                  "entorno": {
                    "temperatura": 23.7,
                    "humedad": 20.16699
                  },
                  "aire": {
                    "co2": 482,
                    "pm1_0": 19.29231,
                    "pm2_5": 32.15385,
                    "pm10": 38.58462
                  }
                }
                """;

        AirQualityMessage message = objectMapper.readValue(json, AirQualityMessage.class);

        assertNotNull(message);
        assertNotNull(message.dispositivo());
        assertEquals("ACEA5AC8E720", message.dispositivo().id());
        assertEquals("Node1", message.dispositivo().nombre());
        assertEquals("1.0.2", message.dispositivo().firmware());
        assertEquals(109, message.dispositivo().secuencia());
        assertEquals(1785274877L, message.dispositivo().timestamp());

        assertNotNull(message.entorno());
        assertEquals(23.7, message.entorno().temperatura());
        assertEquals(20.16699, message.entorno().humedad());

        assertNotNull(message.aire());
        assertEquals(482.0, message.aire().co2());
        assertEquals(19.29231, message.aire().pm1_0());
        assertEquals(32.15385, message.aire().pm2_5());
        assertEquals(38.58462, message.aire().pm10());
    }
}
