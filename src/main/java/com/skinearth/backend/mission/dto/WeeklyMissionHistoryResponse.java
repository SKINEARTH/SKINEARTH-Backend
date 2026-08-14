package com.skinearth.backend.mission.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMissionHistoryResponse(
        LocalDate startDate,
        LocalDate endDate,
        int issuedCount,
        int completedCount,
        double completionRatePercent,
        List<MissionHistoryResponse> cards
) {
}
