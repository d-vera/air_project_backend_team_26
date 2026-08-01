## Why

The backend currently only ingests air quality data via MQTT — there are no REST endpoints to read it back. The dashboard module ("Módulo de Dashboard") requires historical data visualization with time-based aggregation so users can view trends over days, weeks, months, and years. Visitors need public access to recent data, while registered users need full access including custom date ranges.

## What Changes

- Add a new REST controller `AirQualityController` with endpoints for historical and current air quality data
- Implement time-based aggregation using TimescaleDB's `time_bucket()` function with automatic interval selection based on query range
- Add predefined range shortcuts: `LAST_DAY`, `LAST_WEEK`, `LAST_MONTH`, `LAST_YEAR`
- Add custom date range support (`from`/`to` parameters) for authenticated users only
- Support filtering by `deviceId` for multi-device deployments
- Add a "current/latest" endpoint returning the most recent reading per device
- Configure security: visitors can access `LAST_DAY`, `LAST_WEEK`, `LAST_MONTH`; authenticated users can additionally access `LAST_YEAR` and custom ranges

## Capabilities

### New Capabilities
- `historical-data-query`: REST endpoint for querying aggregated historical air quality data with predefined ranges and custom date ranges, including time-bucket-based averaging
- `current-reading-query`: REST endpoint for retrieving the latest air quality reading per device

### Modified Capabilities
- `timescaledb-storage`: Adding native query support with `time_bucket()` aggregation functions to the existing repository

## Impact

- **New files**: `AirQualityController.java`, response DTOs (`HistoricalDataResponse`, `AggregatedReading`, `CurrentReadingResponse`), `TimeRange` enum
- **Modified files**: `AirQualityReadingRepository.java` (add native queries), `AirQualityService.java` (add read methods), `SecurityConfig.java` (permit public access to air-quality endpoints)
- **Dependencies**: Requires TimescaleDB extension with `time_bucket()` function (already set up via `schema.sql`)
- **APIs**: New public and authenticated REST endpoints under `/api/air-quality/`
