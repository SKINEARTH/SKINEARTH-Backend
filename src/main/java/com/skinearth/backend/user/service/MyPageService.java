package com.skinearth.backend.user.service;

import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.service.RecordStreakCalculator;
import com.skinearth.backend.user.dto.MyPageResponse;
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
public class MyPageService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final RecordStreakCalculator recordStreakCalculator;
    private final Clock clock;

    public MyPageResponse get(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        Badge badge = badgeRepository.findByStage(user.getStage())
                .orElseThrow(() -> new NotFoundException("현재 단계의 뱃지 정보를 찾을 수 없습니다."));
        LocalDate today = LocalDate.now(clock);
        int currentStreak = recordStreakCalculator.calculate(
                dailyRecordRepository.findRecordDatesUpTo(userId, today),
                today
        );

        return MyPageResponse.of(user, badge.getName(), currentStreak);
    }
}
