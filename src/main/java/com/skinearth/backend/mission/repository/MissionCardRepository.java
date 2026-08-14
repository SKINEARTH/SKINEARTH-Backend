package com.skinearth.backend.mission.repository;

import com.skinearth.backend.mission.entity.MissionCard;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
