# sensor-management Specification

## Purpose
Provides endpoints and business logic for registering, querying, updating, and deactivating physical air quality sensors, as well as associating sensors with users.

## Requirements

### Requirement: List active sensors
The system SHALL provide a `GET /api/sensors` endpoint to retrieve sensors with coordinates and status. The endpoint SHALL accept an optional query parameter `includeInactive` (boolean, default: `false`). When `includeInactive` is `false` or omitted, the response SHALL contain only active sensors (`active = true`). When `includeInactive` is `true`, the response SHALL contain all sensors (both active and inactive/soft-deleted). The response SHALL contain a list of sensor objects, each including `id`, `uidSensor`, `name`, `sensorType`, `latitude`, `longitude`, `firmwareVersion`, `sensorStatus`, `lastSeen`, `userId`, `active`, `createdAt`, and `updatedAt`.

#### Scenario: Successfully list active sensors by default
- **WHEN** a client sends a GET request to `/api/sensors` without parameters or with `includeInactive=false`
- **THEN** the system SHALL return HTTP 200 with an array of only active sensors including their coordinates, user association (`userId`), and statuses

#### Scenario: Successfully list all sensors including inactive
- **WHEN** a client sends a GET request to `/api/sensors?includeInactive=true`
- **THEN** the system SHALL return HTTP 200 with an array of all sensors (both active and inactive) including their coordinates, user association (`userId`), and statuses


### Requirement: Retrieve sensor by ID
The system SHALL provide a `GET /api/sensors/{id}` endpoint to retrieve details of a specific active sensor by its ID, including the associated `userId`.

#### Scenario: Sensor found
- **WHEN** a client sends a GET request to `/api/sensors/{id}` with an existing active sensor ID
- **THEN** the system SHALL return HTTP 200 with the sensor details and associated `userId`

#### Scenario: Sensor not found
- **WHEN** a client sends a GET request to `/api/sensors/{id}` with a non-existent or inactive sensor ID
- **THEN** the system SHALL return HTTP 404 Not Found

### Requirement: Register a new sensor with User association
The system SHALL provide a `POST /api/sensors` endpoint accessible only to users with role `ADMIN`. The request body SHALL include `uidSensor`, `name`, `sensorType` (optional, defaults to `"ESP32_AIR"`), `latitude`, `longitude`, and optionally `userId` (if omitted, defaults to the authenticated user's ID). The system SHALL validate that `uidSensor` is unique and mandatory, `name` is mandatory, and the referenced `userId` exists.

#### Scenario: Successful sensor registration
- **WHEN** an admin sends a valid POST request to `/api/sensors` with unique `uidSensor`, `name`, `latitude`, `longitude`, and optional `userId`
- **THEN** the system SHALL create the sensor with status `OFFLINE`, associate it with the specified (or authenticated) user, set `active` to true, and return HTTP 201 Created with the saved sensor entity

#### Scenario: Sensor registration with non-existent user
- **WHEN** an admin sends a POST request with a `userId` that does not exist
- **THEN** the system SHALL return HTTP 400 Bad Request with message "User not found"

#### Scenario: Duplicate UID registration
- **WHEN** an admin sends a POST request with a `uidSensor` that is already registered
- **THEN** the system SHALL return HTTP 409 Conflict

#### Scenario: Non-admin attempts sensor registration
- **WHEN** a non-admin user sends a POST request to `/api/sensors`
- **THEN** the system SHALL return HTTP 403 Forbidden

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

### Requirement: Soft delete a sensor
The system SHALL provide a `DELETE /api/sensors/{id}` endpoint accessible only to users with role `ADMIN` to deactivate the sensor by setting `active = false`.

#### Scenario: Successful sensor soft delete
- **WHEN** an admin sends a DELETE request to `/api/sensors/{id}` for an active sensor
- **THEN** the system SHALL set `active = false` and return HTTP 204 No Content

#### Scenario: Soft delete non-existent sensor
- **WHEN** an admin sends a DELETE request to `/api/sensors/{id}` for a non-existent or already deactivated sensor
- **THEN** the system SHALL return HTTP 404 Not Found
