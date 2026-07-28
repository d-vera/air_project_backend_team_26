package com.airproject.airproject.repository;

import com.airproject.airproject.model.AirQualityReading;
import com.airproject.airproject.model.AirQualityReadingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirQualityReadingRepository extends JpaRepository<AirQualityReading, AirQualityReadingId> {
}
