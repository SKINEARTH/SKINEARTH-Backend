package com.skinearth.backend.user.dto;

public record DataResetResponse(
        long deletedDailyRecordCount,
        long deletedForecastCount,
        long deletedMissionCardCount,
        boolean personalizationReset,
        int resetStage,
        int currentStreak
) {
}
