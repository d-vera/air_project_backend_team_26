## MODIFIED Requirements

### Requirement: Process and persist air quality readings
The system SHALL process incoming air quality readings from the MQTT topic, persist them, and update the corresponding sensor's `lastSeen` and `sensorStatus`. When updating sensor status from a reading, the system SHALL check if the sensor is currently in `MAINTENANCE` mode. If the sensor is in `MAINTENANCE`, the system SHALL update `lastSeen` but SHALL NOT change the `sensorStatus` — it SHALL remain as `MAINTENANCE`. If the sensor is not in `MAINTENANCE`, the system SHALL set `sensorStatus` to `ONLINE`.

#### Scenario: Data arrives for sensor not in maintenance
- **WHEN** an MQTT message arrives for a sensor with `sensorStatus` of `ONLINE` or `OFFLINE`
- **THEN** the system SHALL set `sensorStatus` to `ONLINE`, update `lastSeen` to the reading timestamp, and persist the air quality reading

#### Scenario: Data arrives for sensor in maintenance
- **WHEN** an MQTT message arrives for a sensor with `sensorStatus = MAINTENANCE`
- **THEN** the system SHALL update `lastSeen` to the reading timestamp and persist the air quality reading, but SHALL NOT change the `sensorStatus` (it remains `MAINTENANCE`)
