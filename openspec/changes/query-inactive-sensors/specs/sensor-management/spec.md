## MODIFIED Requirements

### Requirement: List active sensors
The system SHALL provide a `GET /api/sensors` endpoint to retrieve sensors with coordinates and status. The endpoint SHALL accept an optional query parameter `includeInactive` (boolean, default: `false`). When `includeInactive` is `false` or omitted, the response SHALL contain only active sensors (`active = true`). When `includeInactive` is `true`, the response SHALL contain all sensors (both active and inactive/soft-deleted). The response SHALL contain a list of sensor objects, each including `id`, `uidSensor`, `name`, `sensorType`, `latitude`, `longitude`, `firmwareVersion`, `sensorStatus`, `lastSeen`, `userId`, `active`, `createdAt`, and `updatedAt`.

#### Scenario: Successfully list active sensors by default
- **WHEN** a client sends a GET request to `/api/sensors` without parameters or with `includeInactive=false`
- **THEN** the system SHALL return HTTP 200 with an array of only active sensors including their coordinates, user association (`userId`), and statuses

#### Scenario: Successfully list all sensors including inactive
- **WHEN** a client sends a GET request to `/api/sensors?includeInactive=true`
- **THEN** the system SHALL return HTTP 200 with an array of all sensors (both active and inactive) including their coordinates, user association (`userId`), and statuses
