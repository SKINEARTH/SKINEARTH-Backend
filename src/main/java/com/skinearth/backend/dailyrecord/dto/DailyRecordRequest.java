package com.skinearth.backend.dailyrecord.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

public record DailyRecordRequest(
        @Min(value = 1, message = "에어컨 노출은 1 이상이어야 합니다.")
        @Max(value = 5, message = "에어컨 노출은 5 이하여야 합니다.")
        Integer acLevel,

        @Min(value = 1, message = "화면 노출은 1 이상이어야 합니다.")
        @Max(value = 5, message = "화면 노출은 5 이하여야 합니다.")
        Integer screenTime,

        @Min(value = 0, message = "수면 시간은 0 이상이어야 합니다.")
        @Max(value = 24, message = "수면 시간은 24 이하여야 합니다.")
        Integer sleepHours,

        @Min(value = 1, message = "스트레스는 1 이상이어야 합니다.")
        @Max(value = 5, message = "스트레스는 5 이하여야 합니다.")
        Integer stressLevel,

        @Min(value = 1, message = "식사 규칙성은 1 이상이어야 합니다.")
        @Max(value = 5, message = "식사 규칙성은 5 이하여야 합니다.")
        Integer mealRegularity,

        @NotNull(message = "피부 컨디션을 입력해주세요.")
        @Min(value = 1, message = "피부 컨디션은 1 이상이어야 합니다.")
        @Max(value = 5, message = "피부 컨디션은 5 이하여야 합니다.")
        Integer skinCondition
) {
    @AssertTrue(message = "환경 요인을 최소 1개 이상 입력해주세요.")
    public boolean isAnyEnvironmentFactorProvided() {
        return acLevel != null
                || screenTime != null
                || sleepHours != null
                || stressLevel != null
                || mealRegularity != null;
    }
}
