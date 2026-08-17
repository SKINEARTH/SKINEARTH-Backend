package com.skinearth.backend.home.service;

import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.service.DailyRecordService;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.home.dto.*;
import com.skinearth.backend.mission.service.MissionCardService;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HomeService {
    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ForecastRepository forecastRepository;
    private final MissionCardService missionCardService;
    private final BadgeService badgeService;
    private final PlanetTemperatureCalculator temperatureCalculator;
    private final Clock clock;

    public HomeResponse get(Long userId) {
        LocalDate today = LocalDate.now(clock);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        DailyRecord todayRecord = dailyRecordRepository.findByUserIdAndRecordDate(userId, today).orElse(null);
        long recordCount = dailyRecordRepository.countByUserId(userId);
        int target = DailyRecordService.FORECAST_TARGET_RECORD_COUNT;

        List<Forecast> recentForecasts = forecastRepository
                .findAllByUser_IdAndTargetDateBetweenOrderByTargetDateAsc(
                        userId, today.minusDays(13), today);
        ForecastResponseDto tomorrowForecast = forecastRepository
                .findByUser_IdAndTargetDate(userId, today.plusDays(1))
                .map(ForecastResponseDto::from)
                .orElse(null);

        return new HomeResponse(
                today,
                user.getNickname(),
                temperatureCalculator.calculate(recentForecasts, today),
                new TodayRecordStatusResponse(todayRecord != null,
                        todayRecord == null ? null : todayRecord.getId(), todayRecord == null),
                progress(recordCount, target),
                tomorrowForecast,
                missionCardService.getTodayCard(userId),
                badgeService.getStageInfo(userId)
        );
    }

    private ForecastProgressResponse progress(long count, int target) {
        long capped = Math.min(count, target);
        return new ForecastProgressResponse(
                count,
                target,
                Math.max(0, target - count),
                (int) Math.round(capped * 100.0 / target),
                count >= target,
                count == target,
                count >= target ? "DATA_BASED" : "ESTIMATED"
        );
    }
}
