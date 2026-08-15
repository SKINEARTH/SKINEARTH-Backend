package com.skinearth.backend.mission.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MissionStreakCalculatorTest {

    private final MissionStreakCalculator calculator = new MissionStreakCalculator();
    private final LocalDate today = LocalDate.of(2026, 8, 16);

    @Test
    void countsCompletedMissionStreakEndingToday() {
        int streak = calculator.calculate(List.of(
                today,
                today.minusDays(1),
                today.minusDays(2)
        ), today);

        assertThat(streak).isEqualTo(3);
    }

    @Test
    void countsYesterdayBasedStreakBeforeTodayMissionIsCompleted() {
        int streak = calculator.calculate(List.of(
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3)
        ), today);

        assertThat(streak).isEqualTo(3);
    }

    @Test
    void resetsWhenYesterdayMissionWasNotCompleted() {
        int streak = calculator.calculate(List.of(
                today.minusDays(2),
                today.minusDays(3)
        ), today);

        assertThat(streak).isZero();
    }

    @Test
    void countsOnlyTodayWhenYesterdayMissionWasNotCompleted() {
        int streak = calculator.calculate(List.of(
                today,
                today.minusDays(2)
        ), today);

        assertThat(streak).isEqualTo(1);
    }
}
