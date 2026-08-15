package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.ForecastFactor;

public record ForecastFactorResponse(String name, String level, Integer priorityScore, int rank) {
    public static ForecastFactorResponse from(ForecastFactor factor) {
        return new ForecastFactorResponse(factor.getFactor().name(), null,
                factor.getPriorityScore(), factor.getFactorRank());
    }
}
