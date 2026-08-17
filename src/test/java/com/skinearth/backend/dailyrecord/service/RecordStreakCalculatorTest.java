package com.skinearth.backend.dailyrecord.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecordStreakCalculatorTest {

    private final RecordStreakCalculator calculator = new RecordStreakCalculator();
    private final LocalDate today = LocalDate.of(2026, 8, 14);

    @Test
    void resetsToZeroWhenPreviousDayRecordDoesNotExist() {
        assertThat(calculator.calculate(List.of(today), today)).isZero();
    }

    @Test
    void countsConsecutiveDatesEndingToday() {
        List<LocalDate> dates = List.of(
                today,
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3)
        );

        assertThat(calculator.calculate(dates, today)).isEqualTo(4);
    }

    @Test
    void stopsAtFirstMissingDate() {
        List<LocalDate> dates = List.of(
                today,
                today.minusDays(1),
                today.minusDays(3),
                today.minusDays(4)
        );

        assertThat(calculator.calculate(dates, today)).isEqualTo(2);
    }

    @Test
    void resetsToZeroEvenWhenOlderRecordsExistButYesterdayIsMissing() {
        List<LocalDate> dates = List.of(today, today.minusDays(2), today.minusDays(3));

        assertThat(calculator.calculate(dates, today)).isZero();
    }

    @Test
    void returnsZeroWhenTodayRecordDoesNotExist() {
        List<LocalDate> dates = List.of(today.minusDays(1), today.minusDays(2));

        assertThat(calculator.calculate(dates, today)).isZero();
    }

    @Test
    void ignoresUnexpectedFutureDates() {
        List<LocalDate> dates = List.of(today.plusDays(1), today, today.minusDays(1));

        assertThat(calculator.calculate(dates, today)).isEqualTo(2);
    }
}
