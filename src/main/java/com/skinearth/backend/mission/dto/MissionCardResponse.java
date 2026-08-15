package com.skinearth.backend.mission.dto;

import com.skinearth.backend.mission.entity.MissionCard;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MissionCardResponse(
        Long id,
        String category,
        String title,
        String description,
        int estimatedMinutes,
        LocalDate issuedDate,
        Boolean isCompleted,
        Boolean isReplaced,
        LocalDateTime completedAt,
        int streak
) {
    public static MissionCardResponse from(MissionCard card, int streak) {
        return new MissionCardResponse(
                card.getId(),
                card.getTemplate().getCategory(),
                card.getTitle(),
                card.getDescription(),
                card.getTemplate().getEstimatedMinutes(),
                card.getIssuedDate(),
                card.getIsCompleted(),
                card.getIsReplaced(),
                card.getCompletedAt(),
                streak
        );
    }
}
