## Context

The backend currently supports soft-deletion (`DELETE /api/sensors/{id}`) by setting `active = false`, and reactivation (`PUT /api/sensors/{id}/reactivate`) for administrator users. However, `GET /api/sensors` unconditionally executes `sensorService.getAllActiveSensors()` which queries `sensorRepository.findByActiveTrue()`. In the frontend admin dashboard, a toggle to "Include Inactive" sensors needs to fetch all sensors, including soft-deleted ones, so administrators can locate and reactivate them.

## Goals / Non-Goals

**Goals:**
- Extend `GET /api/sensors` to accept an optional `includeInactive` query parameter (defaulting to `false`).
- Update `SensorService` to query `sensorRepository.findAll()` when `includeInactive` is `true`, and `sensorRepository.findByActiveTrue()` when `false`.
- Maintain backwards compatibility for callers of `getAllActiveSensors()`.
- Update OpenAPI/Swagger documentation to accurately reflect the parameter and behavior.
- Ensure thorough unit and integration test coverage for both query states (`includeInactive=false` and `includeInactive=true`).

**Non-Goals:**
- Modifying the single sensor lookup endpoint (`GET /api/sensors/{id}`) — that remains unchanged.
- Changing authorization rules for `GET /api/sensors` (both active and inactive sensor listings remain accessible as configured).
- Modifying soft-delete or reactivate behavior.

## Decisions

### 1. Optional `@RequestParam` with default `false` in `SensorController`
- **Choice**: `@RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive`.
- **Rationale**: Keeps existing clients and default calls completely unaffected. Omitting the parameter continues to return active sensors only.
- **Alternatives considered**:
  - Separate endpoint like `GET /api/sensors/all`: Redundant, deviates from RESTful query filtering conventions, and complicates frontend routing.
  - Require role checking for `includeInactive=true`: Sensor metadata itself is not sensitive; keeping the query parameter open on `GET /api/sensors` allows standard consumption while modification/reactivation remains admin-guarded.

### 2. Service method `getAllSensors(boolean includeInactive)`
- **Choice**: Introduce `getAllSensors(boolean includeInactive)` in `SensorService` and retain `getAllActiveSensors()` as a convenience/backwards-compatible method delegating to `getAllSensors(false)` or keeping its existing implementation.
- **Rationale**: Minimizes risk of regressions in existing callers or tests while providing a clean boolean flag interface.

## Risks / Trade-offs

- **[Performance on large sensor sets]** → Querying `sensorRepository.findAll()` returns soft-deleted sensors in addition to active ones. Given the expected scale of sensor devices (hundreds to low thousands), in-memory mapping to `SensorResponse` is lightweight and well within acceptable response times.
- **[Accidental display of inactive sensors in map or user views]** → Defaulting `includeInactive` to `false` ensures standard views (such as public maps or monitoring pages) that do not explicitly pass `includeInactive=true` continue to only receive active sensors.
