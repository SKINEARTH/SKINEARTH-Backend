package com.skinearth.backend.mission.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class MissionStreakCalculator {

    public int calculate(List<LocalDate> completedDates, LocalDate today) {
        boolean completedToday = !completedDates.isEmpty() && completedDates.get(0).equals(today);
        LocalDate expectedDate = completedToday ? today : today.minusDays(1);
        int streak = 0;

        for (LocalDate completedDate : completedDates) {
            if (completedDate.isAfter(expectedDate)) {
                continue;
            }
            if (!completedDate.equals(expectedDate)) {
                break;
            }

            streak++;
            expectedDate = expectedDate.minusDays(1);
        }

        return streak;
    }
}
