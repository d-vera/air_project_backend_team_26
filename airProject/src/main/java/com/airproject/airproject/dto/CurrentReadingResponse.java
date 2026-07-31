package com.airproject.airproject.dto;

import com.airproject.airproject.model.AirQualityReading;

import java.time.Instant;
import java.util.List;

/**
 * Response wrapper for the latest air quality readings per device.
 */
public class CurrentReadingResponse {

    private List<DeviceReading> readings;

    public CurrentReadingResponse() {
    }

    public CurrentReadingResponse(List<DeviceReading> readings) {
        this.readings = readings;
    }

    public List<DeviceReading> getReadings() {
        return readings;
    }

    public void setReadings(List<DeviceReading> readings) {
        this.readings = readings;
    }

    /**
     * Represents the latest reading from a single device.
     */
    public static class DeviceReading {
        private String deviceId;
        private String deviceName;
        private Instant time;
        private Double temperature;
        private Double humidity;
        private Double co2;
        private Double pm1_0;
        private Double pm2_5;
        private Double pm10;

        public DeviceReading() {
        }

        /**
         * Creates a DeviceReading from the JPA entity.
         */
        public static DeviceReading fromEntity(AirQualityReading entity) {
            DeviceReading reading = new DeviceReading();
            reading.deviceId = entity.getDeviceId();
            reading.deviceName = entity.getDeviceName();
            reading.time = entity.getTime();
            reading.temperature = entity.getTemperature();
            reading.humidity = entity.getHumidity();
            reading.co2 = entity.getCo2();
            reading.pm1_0 = entity.getPm1_0();
            reading.pm2_5 = entity.getPm2_5();
            reading.pm10 = entity.getPm10();
            return reading;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Instant getTime() {
            return time;
        }

        public void setTime(Instant time) {
            this.time = time;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getHumidity() {
            return humidity;
        }

        public void setHumidity(Double humidity) {
            this.humidity = humidity;
        }

        public Double getCo2() {
            return co2;
        }

        public void setCo2(Double co2) {
            this.co2 = co2;
        }

        public Double getPm1_0() {
            return pm1_0;
        }

        public void setPm1_0(Double pm1_0) {
            this.pm1_0 = pm1_0;
        }

        public Double getPm2_5() {
            return pm2_5;
        }

        public void setPm2_5(Double pm2_5) {
            this.pm2_5 = pm2_5;
        }

        public Double getPm10() {
            return pm10;
        }

        public void setPm10(Double pm10) {
            this.pm10 = pm10;
        }
    }
}
