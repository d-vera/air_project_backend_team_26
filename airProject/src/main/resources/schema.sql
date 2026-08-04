-- TimescaleDB Extension Initialization and Hypertable Setup
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Convert air_quality_readings table to a hypertable partitioned on time column
SELECT create_hypertable('air_quality_readings', 'time', if_not_exists => TRUE);

-- Create composite index on device_id and time DESC for efficient time-series queries
CREATE INDEX IF NOT EXISTS idx_air_quality_readings_device_time ON air_quality_readings (device_id, time DESC);

-- Ensure preferred_theme and preferred_language columns exist on users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_theme VARCHAR(10) DEFAULT 'DARK';
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(5) DEFAULT 'es';
