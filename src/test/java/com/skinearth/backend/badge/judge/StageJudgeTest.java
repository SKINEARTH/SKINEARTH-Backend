package com.skinearth.backend.badge.judge;

import com.skinearth.backend.badge.entity.Badge;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StageJudgeTest {

    private final StageJudge judge = new StageJudge();

    @Test
    void 기록건수가_기준을_넘으면_2단계_승급_가능() {
        Badge stage2 = Badge.builder()
                .stage(2).name("탐사자")
                .recordCountThreshold(10)
                .build();

        boolean result = judge.isEligibleForNextStage(stage2, 10, 0, 0);

        assertThat(result).isTrue();
    }

    @Test
    void 기록건수가_기준_미달이면_2단계_승급_불가() {
        Badge stage2 = Badge.builder()
                .stage(2).name("탐사자")
                .recordCountThreshold(10)
                .build();

        boolean result = judge.isEligibleForNextStage(stage2, 9, 0, 0);

        assertThat(result).isFalse();
    }

    @Test
    void 스트릭만_기준_넘어도_3단계_승급_가능() {
        Badge stage3 = Badge.builder()
                .stage(3).name("여행자")
                .streakThreshold(7).missionCountThreshold(10)
                .build();

        boolean result = judge.isEligibleForNextStage(stage3, 0, 7, 0);

        assertThat(result).isTrue();
    }

    @Test
    void 미션횟수만_기준_넘어도_3단계_승급_가능() {
        Badge stage3 = Badge.builder()
                .stage(3).name("여행자")
                .streakThreshold(7).missionCountThreshold(10)
                .build();

        boolean result = judge.isEligibleForNextStage(stage3, 0, 0, 10);

        assertThat(result).isTrue();
    }

    @Test
    void 둘_다_미달이면_3단계_승급_불가() {
        Badge stage3 = Badge.builder()
                .stage(3).name("여행자")
                .streakThreshold(7).missionCountThreshold(10)
                .build();

        boolean result = judge.isEligibleForNextStage(stage3, 0, 6, 9);

        assertThat(result).isFalse();
    }
}