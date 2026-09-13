package com.skinearth.backend.home.service;

import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.home.dto.PlanetTemperatureResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PlanetTemperatureCalculator {

    public PlanetTemperatureResponse calculate(
            List<Forecast> forecasts,
            LocalDate today,
            Integer todaySkinCondition
    ) {
        List<Forecast> valid = forecasts.stream()
                .filter(forecast -> forecast.getRiskScore() != null)
                .toList();

        boolean hasTodayForecast = valid.stream()
                .anyMatch(forecast -> today.equals(forecast.getTargetDate()));
        if (!hasTodayForecast && todaySkinCondition != null) {
            int score = skinConditionToRiskScore(todaySkinCondition);
            return new PlanetTemperatureResponse(score, levelOf(score), 1, "DAILY_RECORD");
        }

        if (valid.isEmpty()) {
            return new PlanetTemperatureResponse(null, "데이터 없음", 0, "NO_DATA");
        }

        double weightedSum = 0;
        int weightSum = 0;
        LocalDate recentBoundary = today.minusDays(1);
        for (Forecast forecast : valid) {
            int weight = forecast.getTargetDate().isBefore(recentBoundary) ? 1 : 2;
            weightedSum += forecast.getRiskScore() * weight;
            weightSum += weight;
        }
        int score = (int) Math.round(weightedSum / weightSum);
        return new PlanetTemperatureResponse(score, levelOf(score), valid.size(), "FORECAST");
    }

    private int skinConditionToRiskScore(int skinCondition) {
        if (skinCondition < 1 || skinCondition > 5) {
            throw new IllegalArgumentException("피부 컨디션은 1 이상 5 이하여야 합니다.");
        }
        return (5 - skinCondition) * 25;
    }

    private String levelOf(int score) {
        if (score <= 39) return "안정";
        if (score <= 69) return "주의";
        return "이탈";
    }
}
