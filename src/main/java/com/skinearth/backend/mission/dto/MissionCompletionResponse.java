package com.skinearth.backend.mission.dto;

import java.time.LocalDate;

public record MissionCompletionResponse(
        LocalDate startDate,
        LocalDate endDate,
        int targetCount,
        int issuedCount,
        int completedCount,
        double completionRatePercent,
        MissionAchievementLevel achievementLevel
) {
}
