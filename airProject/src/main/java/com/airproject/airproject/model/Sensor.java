package com.airproject.airproject.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uid_sensor", nullable = false, unique = true, length = 50)
    private String uidSensor;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sensor_type", length = 50)
    @Builder.Default
    private String sensorType = "ESP32_AIR";

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "firmware_version", length = 20)
    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_status", nullable = false, length = 20)
    @Builder.Default
    private SensorStatus sensorStatus = SensorStatus.OFFLINE;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (active == null) {
            active = true;
        }
        if (sensorStatus == null) {
            sensorStatus = SensorStatus.OFFLINE;
        }
        if (sensorType == null) {
            sensorType = "ESP32_AIR";
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
