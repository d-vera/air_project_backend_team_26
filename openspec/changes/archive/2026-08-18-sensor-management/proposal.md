## Why

Currently, the backend processes environmental data from MQTT topics without maintaining a persistent registry or status for physical sensors/nodes. Administrators lack endpoints to register, configure, monitor the physical health of, or manage sensors. Furthermore, sensors need to be associated with the user/administrator who manages or registered them (1-to-N User-to-Sensor relationship), and the frontend dashboard and Google Maps integration require structured sensor metadata including geographic coordinates (`latitude`, `longitude`), firmware versions, and online/offline connectivity status.

## What Changes

- Create a `sensors` table and `Sensor` JPA entity with fields for hardware UID (`uid_sensor`), descriptive `name`, `sensor_type`, geographic coordinates (`latitude`, `longitude`), `firmware_version`, `sensor_status`, `last_seen`, `user_id` (foreign key referencing `users.id`), `active`, and timestamps.
- Implement administrative REST endpoints under `/api/sensors` to create, read, update, list, and soft-delete sensors, associating sensors with the authenticated user/admin.
- Expose public / authenticated endpoints to query sensors with their geographic coordinates and metadata for dashboard and Google Maps visualization.
- Enhance the MQTT ingestion pipeline (`air-quality-ingestion`) to correlate incoming readings with registered sensors, automatically updating `last_seen`, `firmware_version`, and setting `sensor_status` to `ONLINE`.

## Capabilities

### New Capabilities
- `sensor-management`: Comprehensive management and querying of physical sensors, including 1-to-N relationship with `User` (`userId`), CRUD operations, coordinate retrieval for map plotting, and physical status monitoring (`ONLINE`, `OFFLINE`, `MAINTENANCE`).

### Modified Capabilities
- `air-quality-ingestion`: When an MQTT reading arrives, the ingestion service looks up the registered sensor by `uidSensor` (`dispositivo.id`), updates its `last_seen` timestamp, `firmware_version`, and sets its `sensor_status` to `ONLINE`.

## Impact

- **Database**: Adds a new table `sensors` with foreign key `user_id` referencing `users(id)` in PostgreSQL schema.
- **REST API**: Adds new `/api/sensors` endpoints secured with Spring Security (`ADMIN` role for write operations, authenticated/public read operations).
- **MQTT Processing**: `AirQualityService` / `MqttSubscriberService` interacts with `SensorRepository` during reading ingestion.
- **Frontend / Maps**: Provides coordinates, managing user info, and status required by Google Maps markers and sensor status cards.
