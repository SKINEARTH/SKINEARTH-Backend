package com.skinearth.backend.badge.dto;

import com.skinearth.backend.badge.entity.Badge;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BadgeResponseDto {

    private Integer stage;
    private String name;
    private String description;
    private String conditionDescription;
    private List<ProgressItem> progressList;

    @Getter
    @Builder
    public static class ProgressItem {
        private String label;
        private Integer current;
        private Integer target;
    }

    public static BadgeResponseDto from(Badge badge, List<ProgressItem> progressList) {
        return BadgeResponseDto.builder()
                .stage(badge.getStage())
                .name(badge.getName())
                .description(badge.getDescription())
                .conditionDescription(badge.getConditionDescription())
                .progressList(progressList)
                .build();
    }
}