package com.skinearth.backend.dailyrecord.dto;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.entity.SymptomTag;

import java.time.LocalDate;
import java.util.Set;

public record DailyRecordResponse(
        Long id,
        LocalDate recordDate,
        Integer acLevel,
        Integer screenTime,
        Integer sleepHours,
        Integer stressLevel,
        Integer mealRegularity,
        Integer skinCondition,
        Set<SymptomTag> symptoms,
        int currentStreak,
        long validRecordCount,
        int targetRecordCount,
        boolean forecastReady,
        boolean forecastTransitionReached
) {
    public static DailyRecordResponse from(
            DailyRecord record,
            int currentStreak,
            long validRecordCount,
            int targetRecordCount
    ) {
        return new DailyRecordResponse(
                record.getId(),
                record.getRecordDate(),
                record.getAcLevel(),
                record.getScreenTime(),
                record.getSleepHours(),
                record.getStressLevel(),
                record.getMealRegularity(),
                record.getSkinCondition(),
                record.getSymptoms(),
                currentStreak,
                validRecordCount,
                targetRecordCount,
                validRecordCount >= targetRecordCount,
                validRecordCount == targetRecordCount
        );
    }
}
