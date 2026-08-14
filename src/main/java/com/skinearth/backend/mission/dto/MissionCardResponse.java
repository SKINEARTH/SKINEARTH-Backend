package com.skinearth.backend.mission.dto;

import com.skinearth.backend.mission.entity.MissionCard;

import java.time.LocalDate;

public record MissionCardResponse(
        Long id,
        String category,
        String title,
        String description,
        LocalDate issuedDate,
        Boolean isCompleted
) {
    public static MissionCardResponse from(MissionCard card) {
        return new MissionCardResponse(
                card.getId(),
                card.getTemplate().getCategory(),
                card.getTitle(),
                card.getDescription(),
                card.getIssuedDate(),
                card.getIsCompleted()
        );
    }
}