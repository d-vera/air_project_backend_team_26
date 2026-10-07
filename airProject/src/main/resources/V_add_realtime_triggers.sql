-- ====================================================================
-- Real-Time WebSocket Notification Triggers (PostgreSQL LISTEN / NOTIFY)
-- ====================================================================

-- 1. Air Quality Readings Trigger Function & Trigger (AFTER INSERT OR UPDATE)
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


-- 2. Sensor Status Change Trigger Function & Trigger (AFTER INSERT OR UPDATE)
CREATE OR REPLACE FUNCTION notify_sensor_status_change()
RETURNS trigger AS $$
BEGIN
    -- Only notify if inserting, or if sensor_status or active changed
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
