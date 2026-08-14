package com.skinearth.backend.mission.repository;

import com.skinearth.backend.mission.entity.MissionCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MissionCardRepository extends JpaRepository<MissionCard, Long> {
    Optional<MissionCard> findByUserIdAndIssuedDate(Long userId, LocalDate issuedDate);

    long deleteByUserId(Long userId);
}
