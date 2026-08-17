package com.skinearth.backend.home.dto;

public record TodayRecordStatusResponse(
        boolean recorded,
        Long recordId,
        boolean recordCtaRequired
) {
}
