## 1. DTOs and Enum

- [x] 1.1 Create `TimeRange` enum with values `LAST_DAY`, `LAST_WEEK`, `LAST_MONTH`, `LAST_YEAR`
- [x] 1.2 Create `AggregatedReading` projection interface for native query result mapping (`bucket`, `deviceId`, `avgTemperature`, `avgHumidity`, `avgCo2`, `avgPm1_0`, `avgPm2_5`, `avgPm10`)
- [x] 1.3 Create `HistoricalDataResponse` DTO with fields: `range` (from/to), `aggregationInterval`, `data` list
- [x] 1.4 Create `CurrentReadingResponse` DTO wrapping a list of latest readings per device

## 2. Repository Layer

- [x] 2.1 Add native `@Query` to `AirQualityReadingRepository` for aggregated historical data using `time_bucket(:interval, time)` with `device_id` filter
- [x] 2.2 Add native `@Query` to `AirQualityReadingRepository` for latest reading per device using `DISTINCT ON (device_id)` ordered by `time DESC`

## 3. Service Layer

- [x] 3.1 Add method to `AirQualityService` that resolves a `TimeRange` enum to `from`/`to` `Instant` values
- [x] 3.2 Add method to `AirQualityService` that calculates the aggregation interval string based on range duration (≤1 day → '10 minutes', ≤1 week → '1 hour', ≤1 month → '12 hours', else → '24 hours')
- [x] 3.3 Add `getHistoricalData` method to `AirQualityService` that checks authentication, validates parameters, resolves the range, and calls the repository
- [x] 3.4 Add `getCurrentReadings` method to `AirQualityService` that retrieves the latest reading per device (with optional `deviceId` filter)

## 4. Controller Layer

- [x] 4.1 Create `AirQualityController` with `@RequestMapping("/api/air-quality")`
- [x] 4.2 Implement `GET /historical` endpoint accepting `range`, `from`, `to`, and `deviceId` query parameters with Swagger annotations
- [x] 4.3 Implement `GET /current` endpoint accepting optional `deviceId` query parameter with Swagger annotations
- [x] 4.4 Add input validation: mutual exclusivity of `range` vs `from/to`, `from` must be before `to`

## 5. Security Configuration

- [x] 5.1 Update `SecurityConfig` to permit unauthenticated access to `GET /api/air-quality/**`
- [x] 5.2 Implement access control logic in service layer: return 403 for visitors using `LAST_YEAR` or custom ranges

## 6. Testing

- [x] 6.1 Write unit tests for `AirQualityService` aggregation interval calculation and range resolution
- [x] 6.2 Write unit tests for access control logic (visitor vs authenticated user restrictions)
- [x] 6.3 Write integration test verifying the historical endpoint returns correct aggregated data
- [x] 6.4 Write integration test verifying the current endpoint returns latest readings
