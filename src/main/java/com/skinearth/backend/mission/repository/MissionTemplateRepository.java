package com.skinearth.backend.mission.repository;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {
    List<MissionTemplate> findByCauseAndIsActiveTrue(String cause);
}
