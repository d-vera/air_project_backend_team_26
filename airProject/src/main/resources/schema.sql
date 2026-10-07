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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Drop legacy preference columns from users if they exist
ALTER TABLE users DROP COLUMN IF EXISTS preferred_theme;
ALTER TABLE users DROP COLUMN IF EXISTS preferred_language;

-- Create sensors table
CREATE TABLE IF NOT EXISTS sensors (
    id SERIAL PRIMARY KEY,
    uid_sensor VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(50) DEFAULT 'ESP32_AIR',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    firmware_version VARCHAR(20),
    sensor_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_seen TIMESTAMP WITH TIME ZONE,
    user_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sensors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for sensors table
CREATE INDEX IF NOT EXISTS idx_sensors_uid ON sensors (uid_sensor);
CREATE INDEX IF NOT EXISTS idx_sensors_user ON sensors (user_id);

-- Real-Time Notification Trigger Functions and Triggers
CREATE OR REPLACE FUNCTION notify_air_quality_reading()
RETURNS trigger AS $$
BEGIN
    PERFORM pg_notify(
        'air_quality_channel',
        json_build_object(
            'action', TG_OP,
            'id', NEW.id,
            'sensorUid', NEW.device_id,
            'deviceName', NEW.device_name,
            'timestamp', NEW.time,
            'temperature', NEW.temperature,
            'humidity', NEW.humidity,
            'co2', NEW.co2,
            'pm10Small', NEW.pm1_0,
            'pm25', NEW.pm2_5,
            'pm10', NEW.pm10
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_air_quality_reading_change ON air_quality_readings;
CREATE TRIGGER trg_air_quality_reading_change
AFTER INSERT OR UPDATE ON air_quality_readings
FOR EACH ROW EXECUTE FUNCTION notify_air_quality_reading();

CREATE OR REPLACE FUNCTION notify_sensor_status_change()
RETURNS trigger AS $$
BEGIN
    IF (TG_OP = 'INSERT') OR 
       (OLD.sensor_status IS DISTINCT FROM NEW.sensor_status) OR 
       (OLD.active IS DISTINCT FROM NEW.active) THEN
       
        PERFORM pg_notify(
            'sensor_status_channel',
            json_build_object(
                'action', TG_OP,
                'sensorId', NEW.id,
                'uidSensor', NEW.uid_sensor,
                'name', NEW.name,
                'previousStatus', CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.sensor_status END,
                'newStatus', NEW.sensor_status,
                'active', NEW.active,
                'lastSeen', NEW.last_seen
            )::text
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sensor_status_change ON sensors;
CREATE TRIGGER trg_sensor_status_change
AFTER INSERT OR UPDATE ON sensors
FOR EACH ROW EXECUTE FUNCTION notify_sensor_status_change();


