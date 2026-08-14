package com.skinearth.backend.dailyrecord.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecordStreakCalculator {

    public int calculate(List<LocalDate> recordDates, LocalDate today) {
        LocalDate expectedDate = today;
        int streak = 0;

        for (LocalDate recordDate : recordDates) {
            if (recordDate.isAfter(expectedDate)) {
                continue;
            }
            if (!recordDate.equals(expectedDate)) {
                break;
            }

            streak++;
            expectedDate = expectedDate.minusDays(1);
        }

        return streak;
    }
}
