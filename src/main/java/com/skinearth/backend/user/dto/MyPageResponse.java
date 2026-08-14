package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;

import java.time.LocalDate;
import java.util.Set;

public record MyPageResponse(
        String email,
        LocalDate joinedDate,
        String nickname,
        UserStatus userStatus,
        Set<SkinConcern> skinConcerns,
        boolean personalizationCompleted,
        int stage,
        String badgeName,
        int currentStreak
) {
    public static MyPageResponse of(User user, String badgeName, int currentStreak) {
        return new MyPageResponse(
                user.getEmail(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().toLocalDate(),
                user.getNickname(),
                user.getUserStatus(),
                user.getSkinConcerns(),
                user.isPersonalizationCompleted(),
                user.getStage(),
                badgeName,
                currentStreak
        );
    }
}
