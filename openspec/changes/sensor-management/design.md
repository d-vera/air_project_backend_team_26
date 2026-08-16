## Context

The system ingests environmental air quality telemetry (Temperature, Humidity, CO2, PM1.0, PM2.5, PM10) from ESP32 edge devices via MQTT and stores them as time-series data in TimescaleDB. However, the system does not maintain an administrative registry for physical sensor stations, lacking coordinate data (`latitude`, `longitude`) needed for Google Maps visualization, association with managing users (1 to * relationship between `User` and `Sensor`), device tracking, and connectivity health status (`ONLINE`, `OFFLINE`, `MAINTENANCE`).

This design introduces a first-class `Sensor` entity with a foreign key to `User` (`user_id`), an administrative REST controller under `/api/sensors`, a `SensorStatus` enum, and automatic health synchronization during MQTT message ingestion.

## Goals / Non-Goals

**Goals:**
- Create a `sensors` table in PostgreSQL with:
  - `id` (INTEGER / SERIAL PRIMARY KEY)
  - `uid_sensor` (VARCHAR UNIQUE)
  - `name` (VARCHAR)
  - `sensor_type` (VARCHAR)
  - `latitude` (DOUBLE PRECISION)
  - `longitude` (DOUBLE PRECISION)
  - `firmware_version` (VARCHAR)
  - `sensor_status` (VARCHAR)
  - `last_seen` (TIMESTAMPTZ)
  - `user_id` (BIGINT, FK to `users(id)`)
  - `active` (BOOLEAN DEFAULT TRUE)
  - `created_at` and `updated_at` (TIMESTAMPTZ)
- Establish a Many-to-One JPA relationship from `Sensor` to `User` (`@ManyToOne @JoinColumn(name = "user_id")`).
- Create `SensorStatus` Java enum (`ONLINE`, `OFFLINE`, `MAINTENANCE`).
- Create `Sensor` entity, `SensorRepository`, `SensorService`, and `SensorController` (`/api/sensors`).
- Implement CRUD operations for sensors:
  - `GET /api/sensors`: list all active sensors with user information / coordinates (public / authenticated)
  - `GET /api/sensors/{id}`: get single sensor details (public / authenticated)
  - `POST /api/sensors`: create/register new sensor associated with the creating user or specified `userId` (Admin only)
  - `PUT /api/sensors/{id}`: update sensor metadata, coordinates, or assigned user (Admin only)
  - `DELETE /api/sensors/{id}`: soft-delete sensor (Admin only)
- Integrate with `AirQualityService` / `MqttSubscriberService` to automatically update `last_seen`, `firmware_version`, and `sensor_status` (`ONLINE`) when readings arrive from known sensors.

**Non-Goals:**
- Managing individual sub-sensor components (e.g. separate entities for DHT22 vs MQ135 vs PMS5003). The ESP32 device station is modeled as a unified `Sensor` unit.
- Device pre-shared authentication tokens (omitted as per hardware specification).

## Decisions

### 1. User to Sensor Relationship (1 to *)
- **Decision**: Add `user_id` foreign key column to `sensors` table referencing `users(id)`. In JPA, `Sensor` has `@ManyToOne private User user;` and returns `userId` / user details in API responses.
- **Rationale**: Enables tracking which administrator or user registered and is responsible for each physical station.

### 2. Entity and Table Naming
- **Decision**: Name the entity `Sensor` (mapped to table `sensors`) and primary identifier `id` of type `Integer` (`SERIAL` in PostgreSQL).
- **Rationale**: Keeps naming aligned with domain modeling and lightweight footprint for station registries.

### 3. Coordinate Types (`Double` vs `String`)
- **Decision**: Store `latitude` and `longitude` as `Double` in Java and `DOUBLE PRECISION` in PostgreSQL.
- **Rationale**: Direct compatibility with Google Maps API (`LatLngLiteral`), mathematical calculations, range filtering, and spatial bounding box queries without runtime string parsing.

### 4. Automatic Online Status Sync via MQTT Ingestion
- **Decision**: When an MQTT message arrives with `dispositivo.id`, `AirQualityService` will update the corresponding `Sensor`'s `last_seen`, `firmware_version`, and set `sensor_status = ONLINE`.
- **Rationale**: Real-time visibility into sensor physical state without requiring a separate heartbeat daemon.

## Risks / Trade-offs

- **[Risk] Foreign key integrity when user is deleted**:
  - *Mitigation*: Define foreign key constraint `ON DELETE SET NULL` or `RESTRICT` depending on business policy, preserving sensor historical data while soft-deactivating if needed.
- **[Risk] Ingestion latency overhead from updating Sensor entity**:
  - *Mitigation*: Look up `Sensor` by indexed `uid_sensor`.
