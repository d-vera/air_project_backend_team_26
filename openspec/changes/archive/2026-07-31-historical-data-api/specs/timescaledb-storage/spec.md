## MODIFIED Requirements

### Requirement: Air quality readings table exists as a TimescaleDB hypertable
The system SHALL store air quality readings in a PostgreSQL table named `air_quality_readings` with the following columns:

| Column | Type | Constraint |
|--------|------|-----------|
| `id` | BIGINT | PRIMARY KEY, auto-generated |
| `time` | TIMESTAMPTZ | NOT NULL |
| `device_id` | VARCHAR(50) | NOT NULL |
| `device_name` | VARCHAR(100) | |
| `firmware` | VARCHAR(20) | |
| `sequence` | INTEGER | |
| `topic` | VARCHAR(100) | |
| `temperature` | DOUBLE PRECISION | |
| `humidity` | DOUBLE PRECISION | |
| `co2` | DOUBLE PRECISION | |
| `pm1_0` | DOUBLE PRECISION | |
| `pm2_5` | DOUBLE PRECISION | |
| `pm10` | DOUBLE PRECISION | |

A SQL script SHALL be provided to convert this table to a TimescaleDB hypertable partitioned on the `time` column.

The repository SHALL support native SQL queries using TimescaleDB's `time_bucket()` function to aggregate readings by configurable time intervals, grouped by `device_id`.

#### Scenario: Table created by Hibernate
- **WHEN** the application starts with `spring.jpa.hibernate.ddl-auto=update`
- **THEN** Hibernate creates the `air_quality_readings` table with all columns matching the JPA entity

#### Scenario: Hypertable conversion via SQL script
- **WHEN** the DBA runs the provided `schema.sql` script against the database
- **THEN** the `air_quality_readings` table is converted to a TimescaleDB hypertable partitioned on `time`, and an index on `(device_id, time DESC)` is created

#### Scenario: Aggregated query via time_bucket
- **WHEN** the repository is queried with a time range and interval (e.g., '1 hour')
- **THEN** the query uses `time_bucket(:interval, time)` to group readings, computes `AVG()` for temperature, humidity, co2, pm1_0, pm2_5, pm10, and groups by bucket and device_id
