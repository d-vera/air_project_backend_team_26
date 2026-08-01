## ADDED Requirements

### Requirement: Current air quality readings are queryable via REST
The system SHALL expose a REST endpoint `GET /api/air-quality/current` that returns the most recent air quality reading for each device. This endpoint SHALL be publicly accessible without authentication.

#### Scenario: Get current reading for all devices
- **WHEN** a client sends `GET /api/air-quality/current`
- **THEN** the system returns the latest reading per device, including `deviceId`, `deviceName`, `time`, `temperature`, `humidity`, `co2`, `pm1_0`, `pm2_5`, `pm10`

#### Scenario: Get current reading for a specific device
- **WHEN** a client sends `GET /api/air-quality/current?deviceId=ESP32_001`
- **THEN** the system returns only the latest reading for device `ESP32_001`

#### Scenario: No data available
- **WHEN** a client sends `GET /api/air-quality/current` and no readings exist in the database
- **THEN** the system returns HTTP 200 with an empty `readings` array
