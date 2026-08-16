## 1. Database Schema & Migration

- [x] 1.1 Update `schema.sql` to add `sensors` table with `id` (SERIAL PRIMARY KEY), `uid_sensor`, `name`, `sensor_type`, `latitude`, `longitude`, `firmware_version`, `sensor_status`, `last_seen`, `user_id` (BIGINT FK to `users(id)`), `active`, and timestamps
- [x] 1.2 Add index on `uid_sensor` and foreign key constraint to `users(id)`

## 2. Models & Enums

- [x] 2.1 Create `SensorStatus` enum (`ONLINE`, `OFFLINE`, `MAINTENANCE`)
- [x] 2.2 Create `Sensor` entity with JPA annotations, `@ManyToOne User user` relationship, and audit timestamps
- [x] 2.3 Create `SensorRepository` with lookup methods (`findByUidSensor`, `findByActiveTrue`, `findByIdAndActiveTrue`, `findByUserIdAndActiveTrue`)

## 3. DTOs & Validation

- [x] 3.1 Create `CreateSensorRequest` DTO with validation annotations (`@NotBlank`, `@NotNull`, optional `userId`)
- [x] 3.2 Create `UpdateSensorRequest` DTO
- [x] 3.3 Create `SensorResponse` DTO for API responses containing `id`, `uidSensor`, `name`, `sensorType`, `latitude`, `longitude`, `firmwareVersion`, `sensorStatus`, `lastSeen`, `userId`, `active`, and timestamps

## 4. Service Layer

- [x] 4.1 Implement `SensorService` with CRUD methods (`getAllActiveSensors`, `getSensorById`, `createSensor`, `updateSensor`, `deleteSensor`) and User resolution
- [x] 4.2 Update `AirQualityService` / ingestion pipeline to synchronize `Sensor` status (`ONLINE`), `lastSeen`, and `firmwareVersion` when readings arrive

## 5. Controller & Security

- [x] 5.1 Create `SensorController` with `/api/sensors` endpoints
- [x] 5.2 Configure endpoint security permissions in `SecurityConfig` (`GET` accessible to authenticated/public users, `POST`/`PUT`/`DELETE` restricted to `ADMIN`)

## 6. Testing & Verification

- [x] 6.1 Add unit and integration tests for `SensorService` and `SensorController` verifying User association
- [x] 6.2 Test ingestion sync behavior when MQTT readings arrive for registered vs unregistered sensors
