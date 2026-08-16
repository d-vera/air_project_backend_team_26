## MODIFIED Requirements

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
