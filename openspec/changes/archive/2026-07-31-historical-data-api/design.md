## Context

The air quality monitoring backend ingests sensor data via MQTT and stores it in a TimescaleDB hypertable (`air_quality_readings`) partitioned on the `time` column. Currently there are no REST endpoints to query this data. The dashboard module requires both historical trend visualization (with time-based aggregation) and current/latest readings.

The existing stack is Spring Boot 3.x with Spring Data JPA, PostgreSQL/TimescaleDB, JWT authentication, and Swagger documentation. The system supports multiple IoT devices identified by `device_id`.

## Goals / Non-Goals

**Goals:**
- Expose historical air quality data via REST with automatic time-bucket aggregation
- Support predefined range shortcuts (last day/week/month/year) and custom date ranges
- Enforce role-based access: visitors get limited predefined ranges, authenticated users get full access
- Provide a "current reading" endpoint returning the latest data per device
- Leverage TimescaleDB's `time_bucket()` for efficient server-side aggregation
- Support multi-device filtering via `deviceId` query parameter

**Non-Goals:**
- Geographic map visualization (separate change)
- Alerting system (separate change)
- Real-time WebSocket/SSE streaming (future enhancement)
- Data export (CSV, Excel)
- Continuous aggregates or materialized views (optimization for later if needed)

## Decisions

### 1. Use native SQL with `time_bucket()` instead of JPA aggregation

**Decision**: Use `@Query(nativeQuery = true)` with TimescaleDB's `time_bucket()` function.

**Rationale**: TimescaleDB's `time_bucket()` is specifically optimized for hypertable chunk-pruning and time-series aggregation. Doing aggregation in Java would require loading all raw rows into memory — for a year of data at 1 reading/second that's ~31M rows vs. ~365 aggregated rows.

**Alternatives considered**:
- *JPA Criteria API*: No support for `time_bucket()`, would require manual grouping in Java
- *TimescaleDB continuous aggregates*: Better performance but adds schema complexity; can be added later as optimization

### 2. Single endpoint with authorization logic in the service layer

**Decision**: Use one endpoint `GET /api/air-quality/historical` with `permitAll()` access, but enforce range restrictions in the service layer based on authentication status.

**Rationale**: Avoids duplicating endpoint logic between public/private versions. The service checks if the user is authenticated before allowing `LAST_YEAR` or custom `from/to` ranges.

**Alternatives considered**:
- *Separate endpoints* (`/api/air-quality/public/historical` vs `/api/air-quality/historical`): Cleaner security boundaries but duplicated controller/service logic
- *Security expression on parameters*: Spring Security doesn't easily support parameter-level access control

### 3. Automatic interval selection based on range duration

**Decision**: The backend automatically determines the aggregation interval based on the query range duration. No client-side interval selection.

**Rationale**: Keeps the API simple and ensures consistent data density for charting. The rules are:

| Range Duration | Interval | Max points/device |
|---|---|---|
| ≤ 1 day | 10 minutes | ~144 |
| ≤ 1 week | 1 hour | ~168 |
| ≤ 1 month | 12 hours | ~62 |
| ≤ 1 year | 24 hours | ~365 |
| > 1 year | 24 hours | ~365×N |

### 4. Interface-based projection for native query results

**Decision**: Use a Spring Data projection interface to map native query results to DTOs, rather than `Object[]` casting or `@SqlResultSetMapping`.

**Rationale**: Type-safe, minimal boilerplate, and works natively with Spring Data's `@Query`.

### 5. `deviceId` as optional filter, not path variable

**Decision**: Use `?deviceId=ESP32_001` as an optional query parameter. When omitted, return data for all devices.

**Rationale**: Multiple devices may be queried together for dashboard overview. Path variable (`/devices/{id}/historical`) implies single-device focus and makes multi-device queries harder.

## Risks / Trade-offs

- **[Risk] TimescaleDB extension not installed** → The `time_bucket()` queries will fail with SQL errors. Mitigation: validate at startup or fail gracefully with a clear error message. The `schema.sql` script already handles extension creation.

- **[Risk] Large custom ranges (e.g., 5 years) returning too much data** → Even at 24h intervals, 5 years = ~1,825 rows per device. For 100 devices that's 182,500 rows. Mitigation: acceptable for now; can add pagination or device limits later.

- **[Trade-off] No pagination on historical endpoint** → Response size is bounded by the aggregation interval (max ~365 points/device/year), so pagination adds complexity without clear benefit at this scale.

- **[Trade-off] Authorization in service vs. security layer** → Slightly less "pure" from a Spring Security perspective, but avoids endpoint duplication and is easier to test.
