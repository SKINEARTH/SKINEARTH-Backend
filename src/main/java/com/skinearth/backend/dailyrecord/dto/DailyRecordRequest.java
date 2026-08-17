package com.skinearth.backend.dailyrecord.dto;

import com.skinearth.backend.dailyrecord.entity.SymptomTag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.util.HashSet;
import java.util.List;

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
        Integer skinCondition,

        List<@NotNull(message = "증상 태그 값은 null일 수 없습니다.") SymptomTag> symptoms
) {
    @AssertTrue(message = "환경 요인을 최소 1개 이상 입력해주세요.")
    public boolean isAnyEnvironmentFactorProvided() {
        return acLevel != null
                || screenTime != null
                || sleepHours != null
                || stressLevel != null
                || mealRegularity != null;
    }

    @AssertTrue(message = "증상 태그를 중복으로 선택할 수 없습니다.")
    public boolean isSymptomSelectionUnique() {
        return symptoms == null || new HashSet<>(symptoms).size() == symptoms.size();
    }

    @AssertTrue(message = "증상 없음은 다른 증상과 함께 선택할 수 없습니다.")
    public boolean isNoneSelectedAlone() {
        return symptoms == null
                || !symptoms.contains(SymptomTag.NONE)
                || symptoms.size() == 1;
    }
}
