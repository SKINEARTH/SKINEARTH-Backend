package com.skinearth.backend.badge.dto;

import com.skinearth.backend.badge.entity.Badge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeResponseDto {

    private Integer stage;
    private String name;
    private String description;
    private Integer currentProgress;
    private Integer targetThreshold;
    private String progressLabel;

    public static BadgeResponseDto from(Badge badge, Integer currentProgress, Integer targetThreshold) {
        return BadgeResponseDto.builder()
                .stage(badge.getStage())
                .name(badge.getName())
                .description(badge.getDescription())
                .currentProgress(currentProgress)
                .targetThreshold(targetThreshold)
                .progressLabel(targetThreshold != null ? currentProgress + "/" + targetThreshold : null)
                .build();
    }
}