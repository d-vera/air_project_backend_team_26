## 1. Sensor Status — Maintenance Lock in Service Layer

- [x] 1.1 Update `SensorService.updateSensorStatusFromReading()` to skip setting `ONLINE` when sensor is in `MAINTENANCE` mode (still update `lastSeen` and `firmwareVersion`)
- [x] 1.2 Update `SensorServiceTest` with tests for maintenance lock behavior during MQTT data ingestion

## 2. Sensor Auto-Offline Scheduler

- [x] 2.1 Add `@EnableScheduling` to the Spring Boot application configuration
- [x] 2.2 Add `SensorRepository` query method `findByActiveTrueAndSensorStatusAndLastSeenBefore(SensorStatus status, Instant cutoff)` to find stale ONLINE sensors
- [x] 2.3 Create `SensorStatusScheduler` component with `@Scheduled(fixedDelay = 300000)` that marks stale sensors as `OFFLINE`
- [x] 2.4 Add `sensor.offline.threshold-minutes` property to `application.properties` (default: 60)
- [x] 2.5 Create `SensorStatusSchedulerTest` with unit tests covering: stale sensors go offline, maintenance sensors untouched, recent sensors untouched

## 3. Admin Sensor Status Controls

- [x] 3.1 Update `SensorService.updateSensor()` to ensure only admins can manually change `sensorStatus` to any of the three stages: ONLINE, OFFLINE, or MAINTENANCE (already admin-only endpoint). When sensor is in MAINTENANCE, the system cannot auto-override — only admin can transition it out.
- [x] 3.2 Update `SensorControllerTest` with tests for admin setting sensor to each of the 3 statuses (ONLINE, OFFLINE, MAINTENANCE) and transitioning out of MAINTENANCE

## 4. Admin Sensor Soft-Delete and Reactivation

- [x] 4.1 Add `reactivateSensor(Integer id)` method to `SensorService` that sets `active = true` and `sensorStatus = OFFLINE`
- [x] 4.2 Add `PUT /api/sensors/{id}/reactivate` endpoint to `SensorController` (admin-only)
- [x] 4.3 Update `SecurityConfig` to ensure `/api/sensors/*/reactivate` is restricted to `ADMIN` role (already covered by existing `/api/sensors/**` admin rule)
- [x] 4.4 Update `SensorServiceTest` with tests for reactivation (active sensor, inactive sensor, non-existent sensor)
- [x] 4.5 Update `SensorControllerTest` with tests for the reactivate endpoint (success, not found, forbidden)

## 5. Integration and Verification

- [x] 5.1 Run the full test suite (`./mvnw test`) and verify all tests pass
- [x] 5.2 Verify Swagger documentation includes new endpoints and updated descriptions
