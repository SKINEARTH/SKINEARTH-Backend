package com.skinearth.backend.badge.service;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.judge.StageJudge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final MissionCardRepository missionCardRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final StageJudge stageJudge = new StageJudge();

    @Transactional(readOnly = true)
    public BadgeResponseDto getStageInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Badge badge = badgeRepository.findByStage(user.getStage())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 단계입니다."));

        Badge nextBadge = badgeRepository.findByStage(user.getStage() + 1).orElse(null);

        if (nextBadge == null) {
            return BadgeResponseDto.from(badge, null, null);
        }

        Integer recordThreshold = nextBadge.getRecordCountThreshold();
        Integer missionThreshold = nextBadge.getMissionCountThreshold();

        int currentProgress;
        int targetThreshold;

        if (recordThreshold != null) {
            // 2단계: 기록 건수 단일 조건
            currentProgress = (int) dailyRecordRepository.countByUserId(userId);
            targetThreshold = recordThreshold;
        } else {
            // 3단계: OR 조건 중 missionCount 기준으로 단순화
            currentProgress = (int) missionCardRepository.countByUser_IdAndIsCompletedTrue(userId);
            targetThreshold = missionThreshold;
        }

        return BadgeResponseDto.from(badge, currentProgress, targetThreshold);
    }

    @Transactional
    public BadgeResponseDto tryPromote(Long userId, int recordCount, int streakCount) {
        long missionCount = missionCardRepository.countByUser_IdAndIsCompletedTrue(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        int nextStage = user.getStage() + 1;

        Badge nextStageBadge = badgeRepository.findByStage(nextStage)
                .orElse(null);

        if (nextStageBadge != null &&
                stageJudge.isEligibleForNextStage(nextStageBadge, recordCount, streakCount, (int) missionCount)) {
            user.promoteStage();
        }

        Badge currentBadge = badgeRepository.findByStage(user.getStage())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 단계입니다."));

        return BadgeResponseDto.from(currentBadge, null, null);
    }
}