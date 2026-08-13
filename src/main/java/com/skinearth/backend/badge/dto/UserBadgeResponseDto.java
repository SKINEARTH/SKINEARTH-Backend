package com.skinearth.backend.badge.dto;

import com.skinearth.backend.badge.entity.UserBadge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserBadgeResponseDto {

    private String code;
    private String name;
    private String description;
    private LocalDateTime earnedAt;

    public static UserBadgeResponseDto from(UserBadge userBadge) {
        return UserBadgeResponseDto.builder()
                .code(userBadge.getBadge().getCode())
                .name(userBadge.getBadge().getName())
                .description(userBadge.getBadge().getDescription())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}
