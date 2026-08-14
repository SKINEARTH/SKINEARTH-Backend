package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.service.DailyRecordService;
import com.skinearth.backend.user.dto.OnboardingStatusResponse;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingStatusService {

    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;

    public OnboardingStatusResponse get(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        long validRecordCount = dailyRecordRepository.countByUserId(userId);

        return OnboardingStatusResponse.of(
                user,
                validRecordCount,
                DailyRecordService.FORECAST_TARGET_RECORD_COUNT
        );
    }
}
