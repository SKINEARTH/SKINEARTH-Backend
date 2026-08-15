package com.skinearth.backend.forecast.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ForecastRequestDto(
        @NotNull @Min(1) @Max(5) Integer inputAc,
        @NotNull @Min(1) @Max(5) Integer inputScreenTime,
        @NotNull @Min(0) @Max(24) Integer inputSleepHours,
        @NotNull @Min(1) @Max(5) Integer inputStress,
        @NotNull @Min(1) @Max(5) Integer inputMeal
) {
}
