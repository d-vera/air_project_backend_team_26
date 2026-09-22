## Context

The Air Project backend is a Spring Boot application that manages air quality sensors. Sensors report data via MQTT; the `MqttSubscriberService` processes incoming messages and calls `AirQualityService.processAndSave()`, which in turn calls `SensorService.updateSensorStatusFromReading()` to set the sensor to ONLINE and update `lastSeen`.

Currently, once a sensor goes ONLINE there is no mechanism to detect that it has stopped reporting. The `SensorStatus` enum already has `ONLINE`, `OFFLINE`, and `MAINTENANCE` values, but the MAINTENANCE state has no special protection — the system will override it to ONLINE when data arrives. Sensors can be soft-deleted by admins via `DELETE /api/sensors/{id}`, but there is no endpoint to reactivate a soft-deleted sensor.

## Goals / Non-Goals

**Goals:**
- Automatically detect stale sensors (no data for 1 hour) and mark them OFFLINE
- Protect MAINTENANCE status from being overridden by automated processes (scheduler and MQTT ingestion)
- Only allow ADMIN users to manually change a sensor's status between the three stages (ONLINE, OFFLINE, MAINTENANCE)
- Provide an explicit admin-only endpoint for reactivating soft-deleted sensors
- Maintain backward compatibility with all existing API contracts

**Non-Goals:**
- Push notifications or WebSocket alerts when sensor status changes
- Configurable timeout thresholds per sensor (using a fixed 1-hour threshold)
- Any changes to user management endpoints
- Frontend implementation (backend API only)

## Decisions

### Decision 1: Scheduled Task vs. On-Query Evaluation for Auto-Offline

**Chosen**: Spring `@Scheduled` cron job running every 5 minutes

**Alternatives considered**:
- **On-query evaluation** (check `lastSeen` at read time): Would add latency to every GET request and wouldn't update the database state, making it inconsistent across different query paths.
- **Database-level scheduled event**: Would couple logic to the database engine and be harder to test.

**Rationale**: A periodic scheduled task is simple, testable, and keeps the database state consistent. Running every 5 minutes provides a reasonable balance between detection speed and resource usage. Sensors in MAINTENANCE mode will be excluded from the query.

### Decision 2: Maintenance Lock via Status Check in Service Layer

**Chosen**: Guard checks in `SensorService.updateSensorStatusFromReading()` and the new scheduled method

**Rationale**: Rather than adding a separate boolean `maintenanceLocked` field, we simply check if `sensorStatus == MAINTENANCE` before any automated status change. This keeps the model simple and uses the existing enum. Admin status updates via the PUT endpoint bypass this guard since they are intentional manual overrides.

### Decision 3: Explicit Sensor Reactivation Endpoint

**Chosen**: `PUT /api/sensors/{id}/reactivate` as a dedicated admin endpoint

**Alternatives considered**:
- **Reuse existing `PUT /api/sensors/{id}`** with `active: true`: The existing update endpoint does not accept an `active` field, and adding one would mix concerns.

**Rationale**: An explicit endpoint makes the action discoverable in Swagger, is clearer for the frontend team to integrate, and provides a specific audit trail. It complements the existing `DELETE /api/sensors/{id}` soft-delete.

### Decision 4: Scheduler Component Architecture

**Chosen**: New `SensorStatusScheduler` component with `@Scheduled` annotation

**Rationale**: Separating the scheduler into its own `@Component` class follows single-responsibility principle. It depends on `SensorRepository` for the query and `SensorService` or directly the repository for batch updates. Spring's `@EnableScheduling` will be added to the main application or a config class.

## Risks / Trade-offs

- **[Risk] Scheduled job overlap**: If the job takes longer than the interval → Mitigated by using `@Scheduled(fixedDelay = ...)` instead of `fixedRate`, ensuring the next run waits for the current one to complete.
- **[Risk] Clock skew with `lastSeen`**: Sensor timestamps from MQTT may differ from server time → Mitigated by using server-side `Instant.now()` for `lastSeen` in `updateSensorStatusFromReading()` (already the fallback behavior).
- **[Trade-off] Fixed 1-hour threshold**: Not configurable per sensor, but simplifies implementation. Can be externalized to `application.properties` for easy adjustment.
- **[Trade-off] No notification on status change**: Admins must poll or check the dashboard. Push notifications could be added as a future enhancement.
