package com.skinearth.backend.home.service;

import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.home.dto.PlanetTemperatureResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PlanetTemperatureCalculator {

    public PlanetTemperatureResponse calculate(List<Forecast> forecasts, LocalDate today) {
        List<Forecast> valid = forecasts.stream()
                .filter(forecast -> forecast.getRiskScore() != null)
                .toList();
        if (valid.isEmpty()) {
            return new PlanetTemperatureResponse(null, "데이터 없음", 0);
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
        return new PlanetTemperatureResponse(score, levelOf(score), valid.size());
    }

    private String levelOf(int score) {
        if (score <= 39) return "안정";
        if (score <= 69) return "주의";
        return "이탈";
    }
}
