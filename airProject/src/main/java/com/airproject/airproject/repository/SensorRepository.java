package com.airproject.airproject.repository;

import com.airproject.airproject.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Integer> {

    List<Sensor> findByActiveTrue();

    Optional<Sensor> findByIdAndActiveTrue(Integer id);

    Optional<Sensor> findByUidSensor(String uidSensor);

    Optional<Sensor> findByUidSensorAndActiveTrue(String uidSensor);

    boolean existsByUidSensor(String uidSensor);

    List<Sensor> findByUserIdAndActiveTrue(Long userId);
}
