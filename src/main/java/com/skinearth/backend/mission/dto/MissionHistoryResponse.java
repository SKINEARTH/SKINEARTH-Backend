package com.skinearth.backend.mission.dto;

import com.skinearth.backend.mission.entity.MissionCard;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MissionHistoryResponse(
        Long missionCardId,
        String category,
        LocalDate issuedDate,
        boolean completed,
        boolean replaced,
        LocalDateTime completedAt,
        MissionExecutionStatus status
) {
    public static MissionHistoryResponse from(MissionCard card, LocalDate today) {
        MissionExecutionStatus status;
        if (Boolean.TRUE.equals(card.getIsCompleted())) {
            status = MissionExecutionStatus.COMPLETED;
        } else if (card.getIssuedDate().isBefore(today)) {
            status = MissionExecutionStatus.FAILED;
        } else {
            status = MissionExecutionStatus.PENDING;
        }

        return new MissionHistoryResponse(
                card.getId(),
                card.getTemplate().getCategory(),
                card.getIssuedDate(),
                Boolean.TRUE.equals(card.getIsCompleted()),
                Boolean.TRUE.equals(card.getIsReplaced()),
                card.getCompletedAt(),
                status
        );
    }
}
