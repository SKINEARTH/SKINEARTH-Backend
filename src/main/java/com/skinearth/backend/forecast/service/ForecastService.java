package com.skinearth.backend.forecast.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.coldstart.ColdStartCalculator;
import com.skinearth.backend.forecast.coldstart.ColdStartResult;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastService {
    private static final int DATA_BASED_RECORD_COUNT = 10;
    private final ForecastRepository forecastRepository;
    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ColdStartCalculator coldStartCalculator;
    private final Clock clock;

    @Transactional
    public ForecastResponseDto createForecast(Long userId, ForecastRequestDto request) {
        LocalDate targetDate = LocalDate.now(clock).plusDays(1);
        if (forecastRepository.existsByUser_IdAndTargetDate(userId, targetDate))
            throw new IllegalArgumentException("내일의 예보가 이미 존재합니다.");
        User user = findUser(userId);
        if (!user.isPersonalizationCompleted())
            throw new IllegalArgumentException("개인화 설문을 먼저 완료해 주세요.");
        long recordCount = dailyRecordRepository.countByUserId(userId);
        if (recordCount >= DATA_BASED_RECORD_COUNT)
            throw new IllegalArgumentException("유효 기록이 10건 이상이므로 데이터 기반 예보를 이용해야 합니다.");

        ColdStartResult result = coldStartCalculator.calculate(user.getUserStatus(), user.getSkinConcerns(), request);
        Forecast forecast = Forecast.builder()
                .user(user).targetDate(targetDate).inputAc(request.inputAc())
                .inputScreenTime(request.inputScreenTime()).inputSleepHours(request.inputSleepHours())
                .inputStress(request.inputStress()).inputMeal(request.inputMeal())
                .riskScore(result.riskScore()).riskLevel(result.riskLevel())
                .source("COLD_START").validRecordCount((int) recordCount).build();
        forecast.addPrimaryFactors(result.primaryFactors());
        return ForecastResponseDto.from(forecastRepository.save(forecast));
    }

    public ForecastResponseDto getForecast(Long userId) {
        LocalDate targetDate = LocalDate.now(clock).plusDays(1);
        return ForecastResponseDto.from(forecastRepository.findByUser_IdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new NotFoundException("내일의 예보를 찾을 수 없습니다.")));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
