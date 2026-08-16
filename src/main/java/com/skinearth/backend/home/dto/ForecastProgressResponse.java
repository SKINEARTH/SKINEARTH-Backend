package com.skinearth.backend.home.dto;

public record ForecastProgressResponse(
        long validRecordCount,
        int targetRecordCount,
        long remainingRecordCount,
        int progressPercent,
        boolean dataBasedForecastReady,
        boolean forecastTransitionReached,
        String forecastMode
) {
}
