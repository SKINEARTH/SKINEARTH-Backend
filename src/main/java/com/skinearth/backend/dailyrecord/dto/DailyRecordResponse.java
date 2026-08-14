package com.skinearth.backend.dailyrecord.dto;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;

import java.time.LocalDate;

public record DailyRecordResponse(
        Long id,
        LocalDate recordDate,
        Integer acLevel,
        Integer screenTime,
        Integer sleepHours,
        Integer stressLevel,
        Integer mealRegularity,
        Integer skinCondition
) {
    public static DailyRecordResponse from(DailyRecord record) {
        return new DailyRecordResponse(
                record.getId(),
                record.getRecordDate(),
                record.getAcLevel(),
                record.getScreenTime(),
                record.getSleepHours(),
                record.getStressLevel(),
                record.getMealRegularity(),
                record.getSkinCondition()
        );
    }
}
