package com.skinearth.backend.history.dto;

import java.time.LocalDate;

public record HistoryPointResponse(
        LocalDate date,
        Integer skinCondition
) {
}
