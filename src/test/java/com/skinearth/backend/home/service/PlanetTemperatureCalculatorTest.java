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
        ), today, 5);

        assertThat(result.score()).isEqualTo(56);
        assertThat(result.level()).isEqualTo("주의");
        assertThat(result.sampleCount()).isEqualTo(3);
        assertThat(result.source()).isEqualTo("FORECAST");
    }

    @Test
    void returnsNoDataWhenThereIsNoRiskScore() {
        var result = calculator.calculate(List.of(), today, null);
        assertThat(result.score()).isNull();
        assertThat(result.level()).isEqualTo("데이터 없음");
        assertThat(result.source()).isEqualTo("NO_DATA");
    }

    @Test
    void usesTodayRecordWhenTodayForecastDoesNotExist() {
        var result = calculator.calculate(List.of(
                forecast(today.minusDays(1), 70)
        ), today, 4);

        assertThat(result.score()).isEqualTo(25);
        assertThat(result.level()).isEqualTo("안정");
        assertThat(result.sampleCount()).isEqualTo(1);
        assertThat(result.source()).isEqualTo("DAILY_RECORD");
    }

    @Test
    void convertsVeryBadSkinConditionToHighestRiskScore() {
        var result = calculator.calculate(List.of(), today, 1);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.level()).isEqualTo("이탈");
        assertThat(result.source()).isEqualTo("DAILY_RECORD");
    }

    private Forecast forecast(LocalDate date, int score) {
        return Forecast.builder().targetDate(date).riskScore(score).build();
    }
}
