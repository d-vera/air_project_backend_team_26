## ADDED Requirements

### Requirement: Air quality readings table exists as a TimescaleDB hypertable
The system SHALL store air quality readings in a PostgreSQL table named `air_quality_readings` with the following columns:

| Column | Type | Constraint |
|--------|------|-----------|
| `id` | BIGINT | PRIMARY KEY, auto-generated |
| `time` | TIMESTAMPTZ | NOT NULL |
| `device_id` | VARCHAR(50) | NOT NULL |
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

#### Scenario: Table created by Hibernate
- **WHEN** the application starts with `spring.jpa.hibernate.ddl-auto=update`
- **THEN** Hibernate creates the `air_quality_readings` table with all columns matching the JPA entity

#### Scenario: Hypertable conversion via SQL script
- **WHEN** the DBA runs the provided `schema.sql` script against the database
- **THEN** the `air_quality_readings` table is converted to a TimescaleDB hypertable partitioned on `time`, and an index on `(device_id, time DESC)` is created

### Requirement: Database connection parameters are configurable via environment variables
The system SHALL support the following environment variables for PostgreSQL configuration:

| Variable | Default |
|----------|---------|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `airproject` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

#### Scenario: Custom database connection
- **WHEN** the environment variables `DB_HOST=db.example.com`, `DB_PORT=5433`, `DB_NAME=air_prod`, `DB_USERNAME=app_user`, `DB_PASSWORD=secure123` are set
- **THEN** the application connects to `jdbc:postgresql://db.example.com:5433/air_prod` with username `app_user` and password `secure123`

### Requirement: TimescaleDB extension initialization script is provided
The system SHALL include a SQL script (`schema.sql`) that:
1. Creates the TimescaleDB extension if not present
2. Converts the `air_quality_readings` table to a hypertable (idempotent)
3. Creates a composite index on `(device_id, time DESC)` for query performance

#### Scenario: Script executed on fresh database
- **WHEN** `schema.sql` is run after the application has created the table
- **THEN** the TimescaleDB extension is enabled, the table is a hypertable, and the performance index exists
