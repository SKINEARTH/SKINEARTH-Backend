package com.skinearth.backend.home.dto;

public record PlanetTemperatureResponse(
        Integer score,
        String level,
        int sampleCount
) {
}
