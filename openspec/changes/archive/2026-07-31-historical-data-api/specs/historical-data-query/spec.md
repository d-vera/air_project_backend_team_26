## ADDED Requirements

### Requirement: Historical air quality data is queryable via REST with predefined ranges
The system SHALL expose a REST endpoint `GET /api/air-quality/historical` that accepts a `range` query parameter with values `LAST_DAY`, `LAST_WEEK`, `LAST_MONTH`, or `LAST_YEAR` and returns time-aggregated average air quality data for all metrics (temperature, humidity, co2, pm1_0, pm2_5, pm10).

#### Scenario: Query last day of data
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_DAY`
- **THEN** the system returns air quality data averaged in 10-minute buckets for the past 24 hours, grouped by device, ordered by time ascending

#### Scenario: Query last week of data
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_WEEK`
- **THEN** the system returns air quality data averaged in 1-hour buckets for the past 7 days, grouped by device, ordered by time ascending

#### Scenario: Query last month of data
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_MONTH`
- **THEN** the system returns air quality data averaged in 12-hour buckets for the past 31 days, grouped by device, ordered by time ascending

#### Scenario: Query last year of data
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?range=LAST_YEAR`
- **THEN** the system returns air quality data averaged in 24-hour buckets for the past 365 days, grouped by device, ordered by time ascending

### Requirement: Historical air quality data is queryable via REST with custom date ranges
The system SHALL accept optional `from` and `to` query parameters (ISO 8601 format) on the `GET /api/air-quality/historical` endpoint to define a custom date range. The aggregation interval SHALL be automatically selected based on the range duration.

#### Scenario: Custom range of 6 hours
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2026-07-31T00:00:00Z&to=2026-07-31T06:00:00Z`
- **THEN** the system returns air quality data averaged in 10-minute buckets (range ≤ 1 day) for the specified period

#### Scenario: Custom range of 3 days
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2026-07-28T00:00:00Z&to=2026-07-31T00:00:00Z`
- **THEN** the system returns air quality data averaged in 1-hour buckets (range ≤ 1 week) for the specified period

#### Scenario: Custom range of 15 days
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2026-07-15T00:00:00Z&to=2026-07-31T00:00:00Z`
- **THEN** the system returns air quality data averaged in 12-hour buckets (range ≤ 1 month) for the specified period

#### Scenario: Custom range of 6 months
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2026-01-01T00:00:00Z&to=2026-07-01T00:00:00Z`
- **THEN** the system returns air quality data averaged in 24-hour buckets (range ≤ 1 year) for the specified period

#### Scenario: Custom range exceeding one year
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2024-01-01T00:00:00Z&to=2026-07-01T00:00:00Z`
- **THEN** the system returns air quality data averaged in 24-hour buckets (range > 1 year) for the specified period

#### Scenario: Invalid custom range where from is after to
- **WHEN** a client sends `GET /api/air-quality/historical?from=2026-08-01T00:00:00Z&to=2026-07-01T00:00:00Z`
- **THEN** the system returns HTTP 400 Bad Request with an error message

#### Scenario: Both range and custom dates provided
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_DAY&from=2026-07-01T00:00:00Z&to=2026-07-31T00:00:00Z`
- **THEN** the system returns HTTP 400 Bad Request indicating that `range` and `from/to` are mutually exclusive

### Requirement: Historical data can be filtered by device
The system SHALL accept an optional `deviceId` query parameter on the `GET /api/air-quality/historical` endpoint to filter results to a specific device. When omitted, data for all devices SHALL be returned.

#### Scenario: Filter by specific device
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_DAY&deviceId=ESP32_001`
- **THEN** the system returns aggregated data only for device `ESP32_001`

#### Scenario: No device filter
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_DAY`
- **THEN** the system returns aggregated data for all devices

### Requirement: Historical data access is restricted by user role
The system SHALL enforce the following access rules on the historical data endpoint:
- Unauthenticated visitors MAY access `LAST_DAY`, `LAST_WEEK`, and `LAST_MONTH` ranges only
- Unauthenticated visitors SHALL NOT use custom date ranges (`from`/`to`) or `LAST_YEAR`
- Authenticated users (any role) MAY access all ranges and custom date ranges

#### Scenario: Visitor queries last day
- **WHEN** an unauthenticated client sends `GET /api/air-quality/historical?range=LAST_DAY`
- **THEN** the system returns HTTP 200 with aggregated data

#### Scenario: Visitor queries last year
- **WHEN** an unauthenticated client sends `GET /api/air-quality/historical?range=LAST_YEAR`
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Visitor uses custom range
- **WHEN** an unauthenticated client sends `GET /api/air-quality/historical?from=2026-01-01T00:00:00Z&to=2026-07-01T00:00:00Z`
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Authenticated user queries last year
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?range=LAST_YEAR` with a valid JWT
- **THEN** the system returns HTTP 200 with aggregated data

#### Scenario: Authenticated user uses custom range
- **WHEN** an authenticated client sends `GET /api/air-quality/historical?from=2026-01-01T00:00:00Z&to=2026-07-01T00:00:00Z` with a valid JWT
- **THEN** the system returns HTTP 200 with aggregated data

### Requirement: Historical data response includes metadata
The response body SHALL include metadata about the query: the resolved time range (`from`/`to`), the aggregation interval used, and the data array of aggregated readings.

#### Scenario: Response structure for predefined range
- **WHEN** a client sends `GET /api/air-quality/historical?range=LAST_WEEK`
- **THEN** the response body contains `range.from`, `range.to` (ISO 8601), `aggregationInterval` (e.g., "1 hour"), and `data` array with objects containing `bucket`, `deviceId`, `avgTemperature`, `avgHumidity`, `avgCo2`, `avgPm1_0`, `avgPm2_5`, `avgPm10`
