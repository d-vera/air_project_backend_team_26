package com.airproject.airproject.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirQualityMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializePayloadAndIgnoreExtraFields() throws Exception {
        String json = """
                {
                  "device": {
                    "id": "ACEA5AC8E720",
                    "firmware": "3.2.3",
                    "sequence": 7,
                    "timestamp": 1791835722
                  },
                  "stateSensors": {
                    "scd41": 0,
                    "pmsa003i": 0
                  },
                  "environment": {
                    "temperature": 30.99222,
                    "humidity": 22.49485
                  },
                  "airData": {
                    "co2": 687,
                    "pm1_0": 0,
                    "pm2_5": 0,
                    "pm10": 1,
                    "p03": 120
                  }
                }
                """;

        AirQualityMessage message = objectMapper.readValue(json, AirQualityMessage.class);

        assertNotNull(message);
        assertNotNull(message.device());
        assertEquals("ACEA5AC8E720", message.device().id());
        assertEquals("3.2.3", message.device().firmware());
        assertEquals(1791835722L, message.device().timestamp());

        assertNotNull(message.environment());
        assertEquals(30.99222, message.environment().temperature());
        assertEquals(22.49485, message.environment().humidity());

        assertNotNull(message.airData());
        assertEquals(687.0, message.airData().co2());
        assertEquals(0.0, message.airData().pm1_0());
        assertEquals(0.0, message.airData().pm2_5());
        assertEquals(1.0, message.airData().pm10());
    }

    @Test
    void shouldDeserializePayloadWithoutIgnoredFields() throws Exception {
        String json = """
                {
                  "device": {
                    "id": "ACEA5AC8E720",
                    "firmware": "3.2.3",
                    "timestamp": 1791835722
                  },
                  "environment": {
                    "temperature": 30.99222,
                    "humidity": 22.49485
                  },
                  "airData": {
                    "co2": 687,
                    "pm1_0": 0,
                    "pm2_5": 0,
                    "pm10": 1
                  }
                }
                """;

        AirQualityMessage message = objectMapper.readValue(json, AirQualityMessage.class);

        assertNotNull(message);
        assertNotNull(message.device());
        assertEquals("ACEA5AC8E720", message.device().id());
        assertEquals("3.2.3", message.device().firmware());
        assertEquals(1791835722L, message.device().timestamp());

        assertNotNull(message.environment());
        assertEquals(30.99222, message.environment().temperature());
        assertEquals(22.49485, message.environment().humidity());

        assertNotNull(message.airData());
        assertEquals(687.0, message.airData().co2());
        assertEquals(0.0, message.airData().pm1_0());
        assertEquals(0.0, message.airData().pm2_5());
        assertEquals(1.0, message.airData().pm10());
    }
}
