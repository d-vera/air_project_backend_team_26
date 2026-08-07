package com.airproject.airproject.repository;

import com.airproject.airproject.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserEmail(String email);
    Optional<UserPreference> findByUserId(Long userId);
}
