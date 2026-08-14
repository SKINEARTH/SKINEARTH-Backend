package com.skinearth.backend.forecast.repository;

import com.skinearth.backend.forecast.entity.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    Optional<Forecast> findByUser_IdAndTargetDate(Long userId, LocalDate targetDate);
    boolean existsByUser_IdAndTargetDate(Long userId, LocalDate targetDate);
    long deleteByUser_Id(Long userId);
}