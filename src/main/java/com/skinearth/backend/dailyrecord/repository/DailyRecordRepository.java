package com.skinearth.backend.dailyrecord.repository;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {
    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    Optional<DailyRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    @Query("""
            select record.recordDate
            from DailyRecord record
            where record.user.id = :userId
              and record.recordDate <= :date
            order by record.recordDate desc
            """)
    List<LocalDate> findRecordDatesUpTo(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}
