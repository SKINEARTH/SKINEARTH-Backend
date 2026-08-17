package com.skinearth.backend.history.dto;

import java.time.LocalDate;

public record CauseTimelineItemResponse(
        LocalDate startDate,
        LocalDate endDate,
        String factorName,
        String level
) {
}
