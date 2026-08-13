package com.skinearth.backend.badge.service;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
}