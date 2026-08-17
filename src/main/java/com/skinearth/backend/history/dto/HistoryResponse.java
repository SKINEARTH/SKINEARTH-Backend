package com.skinearth.backend.history.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoryResponse(
        HistoryPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        int recordCount,
        Double averageSkinCondition,
        List<HistoryPointResponse> points
) {
}
