-- TimescaleDB Extension Initialization and Hypertable Setup
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Convert air_quality_readings table to a hypertable partitioned on time column
SELECT create_hypertable('air_quality_readings', 'time', if_not_exists => TRUE);

-- Create composite index on device_id and time DESC for efficient time-series queries
CREATE INDEX IF NOT EXISTS idx_air_quality_readings_device_time ON air_quality_readings (device_id, time DESC);

-- Create user_preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL DEFAULT 'ES',
    theme VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Drop legacy preference columns from users if they exist
ALTER TABLE users DROP COLUMN IF EXISTS preferred_theme;
ALTER TABLE users DROP COLUMN IF EXISTS preferred_language;

