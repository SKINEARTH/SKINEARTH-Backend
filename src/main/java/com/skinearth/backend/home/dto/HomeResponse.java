package com.skinearth.backend.home.dto;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.mission.dto.MissionCardResponse;

import java.time.LocalDate;

public record HomeResponse(
        LocalDate date,
        String nickname,
        PlanetTemperatureResponse planetTemperature,
        TodayRecordStatusResponse todayRecord,
        ForecastProgressResponse forecastProgress,
        ForecastResponseDto tomorrowForecast,
        MissionCardResponse todayMission,
        BadgeResponseDto badge
) {
}
