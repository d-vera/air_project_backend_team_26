# admin-sensor-lifecycle Specification

## Purpose
Provides admin-only endpoints for reactivating soft-deleted sensors, complementing the existing soft-delete functionality in sensor-management.

## Requirements

### Requirement: Admin reactivate sensor
The system SHALL provide a `PUT /api/sensors/{id}/reactivate` endpoint accessible only to users with role `ADMIN`. This endpoint SHALL set the target sensor's `active` field to `true` and its `sensorStatus` to `OFFLINE`, allowing the sensor to appear in active sensor listings and begin receiving data again.

#### Scenario: Admin reactivates a soft-deleted sensor
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}/reactivate` for a sensor with `active = false`
- **THEN** the system SHALL set `active = true`, set `sensorStatus = OFFLINE`, update `updatedAt`, and return HTTP 200 with the reactivated sensor details

#### Scenario: Admin reactivates an already active sensor
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}/reactivate` for a sensor with `active = true`
- **THEN** the system SHALL return HTTP 200 with the sensor details (no change needed)

#### Scenario: Non-admin attempts to reactivate a sensor
- **WHEN** a user with role `REGISTERED_USER` sends a PUT request to `/api/sensors/{id}/reactivate`
- **THEN** the system SHALL return HTTP 403 Forbidden

#### Scenario: Reactivate non-existent sensor
- **WHEN** an admin sends a PUT request to `/api/sensors/{id}/reactivate` for a sensor ID that does not exist
- **THEN** the system SHALL return HTTP 404 Not Found with message "Sensor not found with id: {id}"
