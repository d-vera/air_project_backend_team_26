## MODIFIED Requirements

### Requirement: System deserializes incoming MQTT JSON payloads
The system SHALL deserialize incoming MQTT messages from the subscribed topic into a structured DTO matching the following JSON format:

```json
{
  "dispositivo": {
    "id": "ACEA5AC8E720",
    "nombre": "Node1",
    "firmware": "1.0.2",
    "secuencia": 109,
    "Timestamp": 1785274877
  },
  "entorno": { "temperatura": 23.7, "humedad": 20.16699 },
  "aire": { "co2": 482, "pm1_0": 19.29231, "pm2_5": 32.15385, "pm10": 38.58462 }
}
```

#### Scenario: Valid air quality message received
- **WHEN** a valid JSON message is published to `calidad_aire/nodo1`
- **THEN** the system deserializes it into an `AirQualityMessage` DTO with nested `dispositivo` (including `id`, `nombre`, `firmware`, `secuencia`, `Timestamp`), `entorno`, and `aire` objects

#### Scenario: Malformed JSON message received
- **WHEN** a message with invalid JSON is published to the subscribed topic
- **THEN** the system logs an error and continues processing subsequent messages without crashing

### Requirement: System maps DTO to entity and persists to database
The system SHALL map each successfully deserialized `AirQualityMessage` to an `AirQualityReading` entity and persist it to the database. The entity SHALL include:
- `time`: payload `dispositivo.Timestamp` converted to `Instant` (or server `Instant.now()` if `Timestamp` is null)
- `deviceId`: from `dispositivo.id`
- `deviceName`: from `dispositivo.nombre`
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
- **THEN** the system persists an `AirQualityReading` entity with `time` derived from `Timestamp` (or current server timestamp if missing), `deviceName` set to `dispositivo.nombre`, and all sensor fields mapped from the message
