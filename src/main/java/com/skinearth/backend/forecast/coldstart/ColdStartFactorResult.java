package com.skinearth.backend.forecast.coldstart;

public record ColdStartFactorResult(
        ForecastFactorType factor,
        int priorityScore,
        int skinConcernBonus,
        double normalizedRiskValue
) {
}
