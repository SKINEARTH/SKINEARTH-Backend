package com.skinearth.backend.home.service;

import com.skinearth.backend.forecast.entity.Forecast;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanetTemperatureCalculatorTest {
    private final PlanetTemperatureCalculator calculator = new PlanetTemperatureCalculator();
    private final LocalDate today = LocalDate.of(2026, 8, 16);

    @Test
    void appliesDoubleWeightToTodayAndYesterday() {
        var result = calculator.calculate(List.of(
                forecast(today.minusDays(2), 20),
                forecast(today.minusDays(1), 50),
                forecast(today, 80)
        ), today);

        assertThat(result.score()).isEqualTo(56);
        assertThat(result.level()).isEqualTo("주의");
        assertThat(result.sampleCount()).isEqualTo(3);
    }

    @Test
    void returnsNoDataWhenThereIsNoRiskScore() {
        var result = calculator.calculate(List.of(), today);
        assertThat(result.score()).isNull();
        assertThat(result.level()).isEqualTo("데이터 없음");
    }

    private Forecast forecast(LocalDate date, int score) {
        return Forecast.builder().targetDate(date).riskScore(score).build();
    }
}
