## Why

In the frontend Sensor Management dashboard, administrators have an "Include Inactive" toggle to view and reactivate soft-deleted sensors. However, `GET /api/sensors` currently returns only active sensors (`active = true`) without accepting query parameters, preventing administrators from viewing, managing, or reactivating soft-deleted sensors via the UI.

## What Changes

- Add optional query parameter `includeInactive` (boolean, default: `false`) to `GET /api/sensors`.
- When `includeInactive=false` (or omitted), return only active sensors (`active = true`), preserving existing default behavior.
- When `includeInactive=true`, return all sensors (both active and inactive/soft-deleted).
- Update `SensorService` to provide `getAllSensors(boolean includeInactive)` querying either `sensorRepository.findAll()` or `sensorRepository.findByActiveTrue()`, while retaining `getAllActiveSensors()` for backwards compatibility.
- Update OpenAPI/Swagger documentation on `GET /api/sensors` to document the `includeInactive` parameter.
- Add unit tests in `SensorControllerTest` and `SensorServiceTest` covering both `includeInactive=true` and `includeInactive=false`.

## Capabilities

### New Capabilities

<!-- None -->

### Modified Capabilities

- `sensor-management`: Update `List active sensors` requirement to accept an optional `includeInactive` query parameter (default: `false`) to retrieve either all sensors or active-only sensors.

## Impact

- **API**: `GET /api/sensors` accepts optional query parameter `includeInactive` (boolean, default: `false`). Backwards-compatible; default behavior is unchanged.
- **Service Layer**: `SensorService.java` adds `getAllSensors(boolean includeInactive)`.
- **Controller Layer**: `SensorController.java` `getAllSensors` method signature updated with `@RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive`.
- **Tests**: `SensorControllerTest.java` and `SensorServiceTest.java` updated to verify behavior with both `includeInactive=true` and `includeInactive=false`.
- **Documentation**: Swagger/OpenAPI annotations on `GET /api/sensors` updated.
