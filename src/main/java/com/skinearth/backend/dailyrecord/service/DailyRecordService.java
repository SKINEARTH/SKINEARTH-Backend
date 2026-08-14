package com.skinearth.backend.dailyrecord.service;

import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.dto.DailyRecordRequest;
import com.skinearth.backend.dailyrecord.dto.DailyRecordResponse;
import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRecordService {

    public static final int FORECAST_TARGET_RECORD_COUNT = 10;

    private final DailyRecordRepository dailyRecordRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final RecordStreakCalculator recordStreakCalculator;
    private final BadgeService badgeService;

    @Transactional
    public DailyRecordResponse createToday(Long userId, DailyRecordRequest request) {
        LocalDate today = LocalDate.now(clock);
        if (dailyRecordRepository.existsByUserIdAndRecordDate(userId, today)) {
            throw new IllegalArgumentException("오늘의 기록이 이미 존재합니다.");
        }

        User user = findUser(userId);
        DailyRecord record = DailyRecord.builder()
                .user(user)
                .recordDate(today)
                .acLevel(request.acLevel())
                .screenTime(request.screenTime())
                .sleepHours(request.sleepHours())
                .stressLevel(request.stressLevel())
                .mealRegularity(request.mealRegularity())
                .skinCondition(request.skinCondition())
                .symptoms(request.symptoms())
                .build();

        try {
            DailyRecord savedRecord = dailyRecordRepository.saveAndFlush(record);
            DailyRecordResponse response = responseWithStreak(savedRecord, userId, today);
            badgeService.tryPromote(userId, (int) response.validRecordCount(), response.currentStreak());
            return response;
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("오늘의 기록이 이미 존재합니다.");
        }
    }

    public DailyRecordResponse getToday(Long userId) {
        LocalDate today = LocalDate.now(clock);
        return responseWithStreak(findToday(userId), userId, today);
    }

    @Transactional
    public DailyRecordResponse updateToday(Long userId, DailyRecordRequest request) {
        DailyRecord record = findToday(userId);
        record.update(
                request.acLevel(),
                request.screenTime(),
                request.sleepHours(),
                request.stressLevel(),
                request.mealRegularity(),
                request.skinCondition(),
                request.symptoms()
        );
        DailyRecordResponse response = responseWithStreak(record, userId, LocalDate.now(clock));
        badgeService.tryPromote(userId, (int) response.validRecordCount(), response.currentStreak());
        return response;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private DailyRecord findToday(Long userId) {
        return dailyRecordRepository.findByUserIdAndRecordDate(userId, LocalDate.now(clock))
                .orElseThrow(() -> new NotFoundException("오늘의 기록을 찾을 수 없습니다."));
    }

    private DailyRecordResponse responseWithStreak(DailyRecord record, Long userId, LocalDate today) {
        int currentStreak = recordStreakCalculator.calculate(
                dailyRecordRepository.findRecordDatesUpTo(userId, today),
                today
        );
        long validRecordCount = dailyRecordRepository.countByUserId(userId);
        return DailyRecordResponse.from(
                record,
                currentStreak,
                validRecordCount,
                FORECAST_TARGET_RECORD_COUNT
        );
    }
}