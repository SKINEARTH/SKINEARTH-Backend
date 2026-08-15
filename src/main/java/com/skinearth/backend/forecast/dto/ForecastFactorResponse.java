package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.coldstart.ForecastFactorType;
import com.skinearth.backend.forecast.entity.ForecastFactor;

import java.util.Map;

public record ForecastFactorResponse(String name, String level, Integer priorityScore, int rank) {

    private static final Map<ForecastFactorType, String> FACTOR_NAME_KO = Map.of(
            ForecastFactorType.AC, "냉난방",
            ForecastFactorType.SCREEN_TIME, "스크린타임",
            ForecastFactorType.SLEEP, "수면",
            ForecastFactorType.STRESS, "스트레스",
            ForecastFactorType.MEAL_REGULARITY, "식사규칙성"
    );

    public static ForecastFactorResponse from(ForecastFactor factor) {
        return new ForecastFactorResponse(
                FACTOR_NAME_KO.get(factor.getFactor()),
                levelOf(factor.getNormalizedRiskValue()),
                factor.getPriorityScore(),
                factor.getFactorRank()
        );
    }

    private static String levelOf(Double score) {
        if (score == null) return null;
        int rounded = (int) Math.round(score);
        if (rounded <= 39) return "낮음";
        if (rounded <= 69) return "보통";
        return "높음";
    }
}