package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.User;

public record OnboardingStatusResponse(
        String nickname,
        boolean personalizationCompleted,
        long validRecordCount,
        int targetRecordCount,
        long remainingRecordCount,
        boolean firstRecordCompleted,
        boolean forecastReady
) {
    public static OnboardingStatusResponse of(User user, long validRecordCount, int targetRecordCount) {
        return new OnboardingStatusResponse(
                user.getNickname(),
                user.isPersonalizationCompleted(),
                validRecordCount,
                targetRecordCount,
                Math.max(0, targetRecordCount - validRecordCount),
                validRecordCount > 0,
                validRecordCount >= targetRecordCount
        );
    }
}
