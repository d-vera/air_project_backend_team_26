package com.airproject.airproject.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class AirQualityReadingId implements Serializable {

    private Long id;
    private Instant time;

    public AirQualityReadingId() {
    }

    public AirQualityReadingId(Long id, Instant time) {
        this.id = id;
        this.time = time;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirQualityReadingId that = (AirQualityReadingId) o;
        return Objects.equals(id, that.id) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time);
    }
}
