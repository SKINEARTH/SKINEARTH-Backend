package com.skinearth.backend.badge.repository;

import com.skinearth.backend.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByCode(String code);
}