package com.skinearth.backend.dailyrecord.repository;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {
    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    Optional<DailyRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
}
