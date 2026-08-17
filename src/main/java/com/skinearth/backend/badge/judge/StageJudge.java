package com.skinearth.backend.badge.judge;

import com.skinearth.backend.badge.entity.Badge;

public class StageJudge {

    public boolean isEligibleForNextStage(Badge nextStageBadge, int recordCount, int streakCount, int missionCount) {

        Integer recordThreshold = nextStageBadge.getRecordCountThreshold();
        Integer streakThreshold = nextStageBadge.getStreakThreshold();
        Integer missionThreshold = nextStageBadge.getMissionCountThreshold();

        // 기록 건수 조건만 있는 경우 (2단계)
        if (recordThreshold != null && streakThreshold == null && missionThreshold == null) {
            return recordCount >= recordThreshold;
        }

        // 스트릭 OR 미션 조건이 있는 경우 (3단계)
        if (streakThreshold != null || missionThreshold != null) {
            boolean streakMet = streakThreshold != null && streakCount >= streakThreshold;
            boolean missionMet = missionThreshold != null && missionCount >= missionThreshold;
            return streakMet || missionMet;
        }

        return false;
    }
}