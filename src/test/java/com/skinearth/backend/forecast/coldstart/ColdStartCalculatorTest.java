package com.skinearth.backend.forecast.coldstart;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ColdStartCalculatorTest {
    private final ColdStartCalculator calculator = new ColdStartCalculator(new ForecastInputNormalizer());

    @Test
    void accumulatesConcernBonusesAndUsesOnlyTopTwoFactors() {
        var request = new ForecastRequestDto(1, 1, 7, 5, 5);
        var result = calculator.calculate(UserStatus.EMPLOYEE,
                Set.of(SkinConcern.TROUBLE, SkinConcern.PORES), request);

        assertThat(result.primaryFactors()).extracting(ColdStartFactorResult::factor)
                .containsExactly(ForecastFactorType.STRESS, ForecastFactorType.AC);
        assertThat(result.primaryFactors()).extracting(ColdStartFactorResult::priorityScore)
                .containsExactly(6, 6);
        assertThat(result.riskScore()).isEqualTo(50);
        assertThat(result.riskLevel()).isEqualTo("보통");
    }

    @Test
    void otherStatusUsesThreePointsAndDeterministicTieBreaking() {
        var result = calculator.calculate(UserStatus.OTHER, Set.of(),
                new ForecastRequestDto(5, 1, 7, 1, 5));
        assertThat(result.primaryFactors()).extracting(ColdStartFactorResult::factor)
                .containsExactly(ForecastFactorType.STRESS, ForecastFactorType.AC);
        assertThat(result.riskScore()).isEqualTo(50);
    }
}
