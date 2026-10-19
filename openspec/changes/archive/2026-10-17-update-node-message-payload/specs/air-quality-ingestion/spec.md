## MODIFIED Requirements

### Requirement: System deserializes incoming MQTT JSON payloads
The system SHALL deserialize incoming MQTT messages from the subscribed topic into a structured DTO matching the following JSON format:

```json
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
```

The system SHALL ignore the following fields if present in the payload:
- `device.sequence` — not consumed by the backend.
- `stateSensors` — sensor hardware status is not consumed by the backend.
- `airData.p03` — particle count ≥0.3µm is not consumed by the backend.

Unknown fields at any nesting level SHALL be silently ignored (the system SHALL NOT fail on unknown properties).

#### Scenario: Valid air quality message received with new format
- **WHEN** a valid JSON message with the new English-language structure is published to `calidad_aire/nodo1`
- **THEN** the system deserializes it into an `AirQualityMessage` DTO with nested `device` (including `id`, `firmware`, `timestamp`), `environment` (including `temperature`, `humidity`), and `airData` (including `co2`, `pm1_0`, `pm2_5`, `pm10`) objects

#### Scenario: Message with ignored fields received
- **WHEN** a valid JSON message containing `stateSensors`, `device.sequence`, or `airData.p03` fields is published
- **THEN** the system deserializes the message successfully, silently ignoring those fields

#### Scenario: Malformed JSON message received
- **WHEN** a message with invalid JSON is published to the subscribed topic
- **THEN** the system logs an error and continues processing subsequent messages without crashing

### Requirement: System maps DTO to entity and persists to database
The system SHALL map each successfully deserialized `AirQualityMessage` to an `AirQualityReading` entity and persist it to the database. The entity SHALL include:
- `time`: payload `device.timestamp` converted to `Instant` (or server `Instant.now()` if `timestamp` is null or zero)
- `deviceId`: from `device.id`
- `deviceName`: set to `null` (no longer provided by sensor nodes)
- `firmware`: from `device.firmware`
- `sequence`: set to `null` (explicitly ignored)
- `topic`: the MQTT topic the message arrived on
- `temperature`: from `environment.temperature`
- `humidity`: from `environment.humidity`
- `co2`: from `airData.co2`
- `pm1_0`: from `airData.pm1_0`
- `pm2_5`: from `airData.pm2_5`
- `pm10`: from `airData.pm10`

Additionally, upon processing the message, the system SHALL check if a `Sensor` entity exists with `uidSensor` matching `device.id`. If found, the system SHALL update the sensor's `lastSeen` timestamp to the reading time (or current time) and update `firmwareVersion` if provided. When updating sensor status from a reading, the system SHALL check if the sensor is currently in `MAINTENANCE` mode. If the sensor is in `MAINTENANCE`, the system SHALL update `lastSeen` but SHALL NOT change the `sensorStatus` — it SHALL remain as `MAINTENANCE`. If the sensor is not in `MAINTENANCE`, the system SHALL set `sensorStatus` to `ONLINE`.

#### Scenario: Message successfully persisted and sensor status updated
- **WHEN** a valid air quality message is received and deserialized for a registered sensor with `sensorStatus` of `ONLINE` or `OFFLINE`
- **THEN** the system persists an `AirQualityReading` entity with `deviceName=null` and `sequence=null`, and updates the corresponding `Sensor`'s `lastSeen` timestamp, `firmwareVersion`, and sets `sensorStatus` to `ONLINE`

#### Scenario: Message persisted for sensor in maintenance
- **WHEN** a valid air quality message is received for a registered sensor with `sensorStatus = MAINTENANCE`
- **THEN** the system persists the `AirQualityReading` entity, updates `lastSeen`, but SHALL NOT change the `sensorStatus` (it remains `MAINTENANCE`)

#### Scenario: Message from unregistered sensor
- **WHEN** a valid air quality message is received for a `device.id` not present in `sensors`
- **THEN** the system persists the `AirQualityReading` normally and logs a warning that the sensor is unregistered
