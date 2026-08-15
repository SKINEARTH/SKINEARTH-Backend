package com.skinearth.backend.badge.service;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.dto.BadgeResponseDto.ProgressItem;
import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.judge.StageJudge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.service.RecordStreakCalculator;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final MissionCardRepository missionCardRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final RecordStreakCalculator recordStreakCalculator;
    private final Clock clock;
    private final StageJudge stageJudge = new StageJudge();

    @Transactional(readOnly = true)
    public BadgeResponseDto getStageInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Badge badge = badgeRepository.findByStage(user.getStage())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 단계입니다."));

        Badge nextBadge = badgeRepository.findByStage(user.getStage() + 1).orElse(null);

        if (nextBadge == null) {
            return BadgeResponseDto.from(badge, Collections.emptyList());
        }

        List<ProgressItem> progressList = new ArrayList<>();

        Integer recordThreshold = nextBadge.getRecordCountThreshold();
        Integer streakThreshold = nextBadge.getStreakThreshold();
        Integer missionThreshold = nextBadge.getMissionCountThreshold();

        if (recordThreshold != null) {
            // 2단계: 기록 건수 단일 조건
            int recordCurrent = (int) dailyRecordRepository.countByUserId(userId);
            progressList.add(ProgressItem.builder()
                    .label("궤도를 기록하기")
                    .current(recordCurrent)
                    .target(recordThreshold)
                    .build());
        } else {
            // 3단계: 스트릭 조건 + 미션 조건, 둘 다 각각 표시
            if (streakThreshold != null) {
                LocalDate today = LocalDate.now(clock);
                int streakCurrent = recordStreakCalculator.calculate(
                        dailyRecordRepository.findRecordDatesUpTo(userId, today), today);
                progressList.add(ProgressItem.builder()
                        .label("궤도 연속 기록하기")
                        .current(streakCurrent)
                        .target(streakThreshold)
                        .build());
            }
            if (missionThreshold != null) {
                int missionCurrent = (int) missionCardRepository.countByUser_IdAndIsCompletedTrue(userId);
                progressList.add(ProgressItem.builder()
                        .label("탐사 미션 완료하기")
                        .current(missionCurrent)
                        .target(missionThreshold)
                        .build());
            }
        }

        return BadgeResponseDto.from(badge, progressList);
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

        return BadgeResponseDto.from(currentBadge, Collections.emptyList());
    }
}