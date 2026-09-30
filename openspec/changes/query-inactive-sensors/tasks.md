## 1. Service Layer

- [x] 1.1 Add `getAllSensors(boolean includeInactive)` in `SensorService` to query `sensorRepository.findAll()` when `true` and `sensorRepository.findByActiveTrue()` when `false`
- [x] 1.2 Retain `getAllActiveSensors()` in `SensorService` delegating to `getAllSensors(false)` for backwards compatibility

## 2. Controller & API Documentation

- [x] 2.1 Update `getAllSensors` in `SensorController` to accept `@RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive` and delegate to `sensorService.getAllSensors(includeInactive)`
- [x] 2.2 Update OpenAPI `@Operation`, `@Parameter`, and `@ApiResponse` annotations on `getAllSensors` in `SensorController`

## 3. Automated Testing & Verification

- [x] 3.1 Update `SensorServiceTest` to test `getAllSensors(true)` and `getAllSensors(false)`
- [x] 3.2 Update `SensorControllerTest` to test `getAllSensors(true)` and `getAllSensors(false)` / default invocation
- [x] 3.3 Execute full test suite via `./mvnw test` to ensure all tests pass with zero regressions
