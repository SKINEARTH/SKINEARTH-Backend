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

    public static BadgeResponseDto from(Badge badge) {
        return BadgeResponseDto.builder()
                .stage(badge.getStage())
                .name(badge.getName())
                .description(badge.getDescription())
                .build();
    }
}