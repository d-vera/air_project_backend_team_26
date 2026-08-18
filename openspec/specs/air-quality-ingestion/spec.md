# air-quality-ingestion Specification

## Purpose
TBD - created by archiving change emqx-mqtt-integration. Update Purpose after archive.
## Requirements
### Requirement: System subscribes to MQTT topic on startup
The system SHALL subscribe to the configured MQTT topic (`calidad_aire/nodo1` by default) with the configured QoS level immediately after the MQTT client connects.

#### Scenario: Successful subscription on startup
- **WHEN** the application starts and the MQTT client connects successfully
- **THEN** the system subscribes to the topic defined by the `MQTT_TOPIC` environment variable with QoS defined by `MQTT_QOS`

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

Additionally, upon processing the message, the system SHALL check if a `Sensor` entity exists with `uidSensor` matching `dispositivo.id`. If found, the system SHALL update the sensor's `lastSeen` timestamp to the reading time (or current time), update `firmwareVersion` if provided, and set `sensorStatus` to `ONLINE`.

#### Scenario: Message successfully persisted and sensor status updated
- **WHEN** a valid air quality message is received and deserialized for a registered sensor
- **THEN** the system persists an `AirQualityReading` entity and updates the corresponding `Sensor`'s `lastSeen` timestamp, `firmwareVersion`, and sets `sensorStatus` to `ONLINE`

#### Scenario: Message from unregistered sensor
- **WHEN** a valid air quality message is received for a `dispositivo.id` not present in `sensors`
- **THEN** the system persists the `AirQualityReading` normally and logs a warning that the sensor is unregistered

### Requirement: System logs each successfully ingested message
The system SHALL log a confirmation message at INFO level after each successful persistence, including the device ID and topic.

#### Scenario: Ingestion logged
- **WHEN** an air quality reading is successfully saved to the database
- **THEN** the system logs an INFO message containing the device ID and topic name

