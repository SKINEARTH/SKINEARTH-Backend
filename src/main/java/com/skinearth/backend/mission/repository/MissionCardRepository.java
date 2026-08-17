package com.skinearth.backend.mission.repository;

import com.skinearth.backend.mission.entity.MissionCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MissionCardRepository extends JpaRepository<MissionCard, Long> {
    Optional<MissionCard> findByUser_IdAndIssuedDate(Long userId, LocalDate issuedDate);

    Optional<MissionCard> findByIdAndUser_Id(Long id, Long userId);

    List<MissionCard> findAllByUser_IdAndIssuedDateBetweenOrderByIssuedDateDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    long deleteByUser_Id(Long userId);

    long countByUser_IdAndIsCompletedTrue(Long userId);

    @Query("""
            select card.issuedDate
            from MissionCard card
            where card.user.id = :userId
              and card.isCompleted = true
              and card.issuedDate <= :date
            order by card.issuedDate desc
            """)
    List<LocalDate> findCompletedDatesUpTo(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}
