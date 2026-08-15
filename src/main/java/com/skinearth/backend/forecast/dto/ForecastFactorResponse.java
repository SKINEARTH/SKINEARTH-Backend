package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.ForecastFactor;

public record ForecastFactorResponse(String factor, Integer priorityScore, int rank) {
    public static ForecastFactorResponse from(ForecastFactor factor) {
        return new ForecastFactorResponse(factor.getFactor().name(), factor.getPriorityScore(), factor.getFactorRank());
    }
}
