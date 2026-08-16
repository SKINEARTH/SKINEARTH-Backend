package com.skinearth.backend.mission.dto;

import java.time.LocalDate;

public record MonthlyMissionCompletionResponse(
        LocalDate startDate,
        LocalDate endDate,
        int issuedCount,
        int completedCount,
        double completionRatePercent
) {
}
