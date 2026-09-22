## MODIFIED Requirements

### Requirement: Update an existing sensor
The system SHALL provide a `PUT /api/sensors/{id}` endpoint accessible only to users with role `ADMIN` to update sensor details (`name`, `sensorType`, `latitude`, `longitude`, `sensorStatus`, and/or `userId`). Only users with role `ADMIN` SHALL be permitted to manually change a sensor's `sensorStatus` to any of the three stages: `ONLINE`, `OFFLINE`, or `MAINTENANCE`. The system SHALL allow admins to transition a sensor from `MAINTENANCE` to any other status (`ONLINE` or `OFFLINE`). When a sensor is in `MAINTENANCE`, only an admin can change its status — the system SHALL NOT automatically override it.

#### Scenario: Successful sensor update
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}` with updated fields
- **THEN** the system SHALL update the sensor, refresh `updatedAt`, and return HTTP 200 with the updated sensor

#### Scenario: Admin sets sensor to MAINTENANCE
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}` with `sensorStatus: MAINTENANCE`
- **THEN** the system SHALL set the sensor's status to `MAINTENANCE` and return HTTP 200

#### Scenario: Admin transitions sensor out of MAINTENANCE
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}` for a sensor in MAINTENANCE with `sensorStatus: ONLINE` or `sensorStatus: OFFLINE`
- **THEN** the system SHALL update the sensor's status and return HTTP 200

#### Scenario: Update non-existent sensor
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}` for a sensor ID that does not exist
- **THEN** the system SHALL return HTTP 404 Not Found
