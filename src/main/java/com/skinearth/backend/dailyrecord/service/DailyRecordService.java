package com.skinearth.backend.dailyrecord.service;

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

    private final DailyRecordRepository dailyRecordRepository;
    private final UserRepository userRepository;
    private final Clock clock;

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
            return DailyRecordResponse.from(dailyRecordRepository.saveAndFlush(record));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("오늘의 기록이 이미 존재합니다.");
        }
    }

    public DailyRecordResponse getToday(Long userId) {
        return DailyRecordResponse.from(findToday(userId));
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
        return DailyRecordResponse.from(record);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private DailyRecord findToday(Long userId) {
        return dailyRecordRepository.findByUserIdAndRecordDate(userId, LocalDate.now(clock))
                .orElseThrow(() -> new NotFoundException("오늘의 기록을 찾을 수 없습니다."));
    }
}
