package com.skinearth.backend.badge.service;

import com.skinearth.backend.badge.dto.UserBadgeResponseDto;
import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.entity.UserBadge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.badge.repository.UserBadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public List<UserBadgeResponseDto> getUserBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        return userBadges.stream()
                .map(UserBadgeResponseDto::from)
                .toList();
    }

    public void tryAwardBadge(Long userId, String badgeCode, int actualCount) {
        Badge badge = badgeRepository.findByCode(badgeCode)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 뱃지입니다."));

        if (userBadgeRepository.existsByUserIdAndBadge_Code(userId, badgeCode)) {
            return;
        }

        if (isEligible(actualCount, badge.getThreshold())) {
            UserBadge userBadge = UserBadge.builder()
                    .userId(userId)
                    .badge(badge)
                    .build();
            userBadgeRepository.save(userBadge);
        }
    }

    private boolean isEligible(int actualCount, int threshold) {
        return actualCount >= threshold;
    }
}