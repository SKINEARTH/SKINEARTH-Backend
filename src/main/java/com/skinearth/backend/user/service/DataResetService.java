package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.user.dto.DataResetResponse;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataResetService {

    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ForecastRepository forecastRepository;
    private final MissionCardRepository missionCardRepository;

    @Transactional
    public DataResetResponse reset(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        long deletedDailyRecordCount = dailyRecordRepository.deleteByUserId(userId);
        long deletedForecastCount = forecastRepository.deleteByUser_Id(userId);
        long deletedMissionCardCount = missionCardRepository.deleteByUser_Id(userId);
        user.resetServiceData();

        return new DataResetResponse(
                deletedDailyRecordCount,
                deletedForecastCount,
                deletedMissionCardCount,
                true,
                user.getStage(),
                0
        );
    }
}
