package com.skinearth.backend.forecast.coldstart;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class ForecastInputNormalizer {

    public Map<ForecastFactorType, Double> normalize(ForecastRequestDto request) {
        Map<ForecastFactorType, Double> values = new EnumMap<>(ForecastFactorType.class);
        values.put(ForecastFactorType.AC, normalizeDirect(request.inputAc()));
        values.put(ForecastFactorType.SCREEN_TIME, normalizeDirect(request.inputScreenTime()));
        values.put(ForecastFactorType.STRESS, normalizeDirect(request.inputStress()));
        values.put(ForecastFactorType.MEAL_REGULARITY, normalizeMeal(request.inputMeal()));
        values.put(ForecastFactorType.SLEEP, normalizeSleep(request.inputSleepHours()));
        return values;
    }

    double normalizeDirect(int value) {
        return (value - 1) / 4.0 * 100.0;
    }

    double normalizeMeal(int value) {
        return (5 - value) / 4.0 * 100.0;
    }

    double normalizeSleep(int hours) {
        return hours < 7 ? (7 - hours) / 7.0 * 100.0 : 0.0;
    }
}
