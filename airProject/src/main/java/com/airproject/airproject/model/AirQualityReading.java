package com.airproject.airproject.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "air_quality_readings")
@IdClass(AirQualityReadingId.class)
public class AirQualityReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    @Column(name = "time", nullable = false)
    private Instant time;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "firmware", length = 20)
    private String firmware;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "co2")
    private Double co2;

    @Column(name = "pm1_0")
    private Double pm10Small; // pm1_0

    @Column(name = "pm2_5")
    private Double pm25; // pm2_5

    @Column(name = "pm10")
    private Double pm10; // pm10

    public AirQualityReading() {
    }

    public AirQualityReading(Instant time, String deviceId, String firmware, Integer sequence, String topic,
                             Double temperature, Double humidity, Double co2, Double pm10Small, Double pm25, Double pm10) {
        this.time = time;
        this.deviceId = deviceId;
        this.firmware = firmware;
        this.sequence = sequence;
        this.topic = topic;
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.pm10Small = pm10Small;
        this.pm25 = pm25;
        this.pm10 = pm10;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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
        return pm10Small;
    }

    public void setPm1_0(Double pm10Small) {
        this.pm10Small = pm10Small;
    }

    public Double getPm2_5() {
        return pm25;
    }

    public void setPm2_5(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }
}
