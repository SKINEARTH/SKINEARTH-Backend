package com.skinearth.backend.mission.dto;

public record MissionAlternativeResponse(
        String title,
        String description,
        String category,
        int estimatedMinutes
) {
}
