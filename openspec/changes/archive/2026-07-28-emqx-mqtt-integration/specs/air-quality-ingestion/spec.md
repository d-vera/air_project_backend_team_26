## ADDED Requirements

### Requirement: System subscribes to MQTT topic on startup
The system SHALL subscribe to the configured MQTT topic (`calidad_aire/nodo1` by default) with the configured QoS level immediately after the MQTT client connects.

#### Scenario: Successful subscription on startup
- **WHEN** the application starts and the MQTT client connects successfully
- **THEN** the system subscribes to the topic defined by the `MQTT_TOPIC` environment variable with QoS defined by `MQTT_QOS`

### Requirement: System deserializes incoming MQTT JSON payloads
The system SHALL deserialize incoming MQTT messages from the subscribed topic into a structured DTO matching the following JSON format:

```json
{
  "dispositivo": { "id": "node1", "firmware": "1.0.0", "secuencia": 120 },
  "entorno": { "temperatura": 22.24, "humedad": 35.96289 },
  "aire": { "co2": 454, "pm1_0": 17.11648, "pm2_5": 28.52747, "pm10": 34.23297 }
}
```

#### Scenario: Valid air quality message received
- **WHEN** a valid JSON message is published to `calidad_aire/nodo1`
- **THEN** the system deserializes it into an `AirQualityMessage` DTO with nested `dispositivo`, `entorno`, and `aire` objects

#### Scenario: Malformed JSON message received
- **WHEN** a message with invalid JSON is published to the subscribed topic
- **THEN** the system logs an error and continues processing subsequent messages without crashing

### Requirement: System maps DTO to entity and persists to database
The system SHALL map each successfully deserialized `AirQualityMessage` to an `AirQualityReading` entity and persist it to the database. The entity SHALL include:
- `time`: server-side arrival timestamp (`Instant.now()`)
- `deviceId`: from `dispositivo.id`
- `firmware`: from `dispositivo.firmware`
- `sequence`: from `dispositivo.secuencia`
- `topic`: the MQTT topic the message arrived on
- `temperature`: from `entorno.temperatura`
- `humidity`: from `entorno.humedad`
- `co2`: from `aire.co2`
- `pm1_0`: from `aire.pm1_0`
- `pm2_5`: from `aire.pm2_5`
- `pm10`: from `aire.pm10`

#### Scenario: Message successfully persisted
- **WHEN** a valid air quality message is received and deserialized
- **THEN** the system persists an `AirQualityReading` entity with `time` set to the current server timestamp and all sensor fields mapped from the message

### Requirement: System logs each successfully ingested message
The system SHALL log a confirmation message at INFO level after each successful persistence, including the device ID and topic.

#### Scenario: Ingestion logged
- **WHEN** an air quality reading is successfully saved to the database
- **THEN** the system logs an INFO message containing the device ID and topic name
