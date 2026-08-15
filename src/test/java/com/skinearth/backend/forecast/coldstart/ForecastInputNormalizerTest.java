package com.skinearth.backend.forecast.coldstart;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastInputNormalizerTest {
    private final ForecastInputNormalizer normalizer = new ForecastInputNormalizer();

    @Test
    void normalizesAllInputsIntoRiskOrientedScores() {
        var result = normalizer.normalize(new ForecastRequestDto(5, 3, 3, 1, 5));
        assertThat(result.get(ForecastFactorType.AC)).isEqualTo(100.0);
        assertThat(result.get(ForecastFactorType.SCREEN_TIME)).isEqualTo(50.0);
        assertThat(result.get(ForecastFactorType.SLEEP)).isCloseTo(400.0 / 7.0, within(0.0001));
        assertThat(result.get(ForecastFactorType.STRESS)).isZero();
        assertThat(result.get(ForecastFactorType.MEAL_REGULARITY)).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
