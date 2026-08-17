package com.skinearth.backend.mission.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MissionAchievementLevelTest {

    @Test
    void classifiesProgressByCompletionRate() {
        assertThat(MissionAchievementLevel.from(70.0)).isEqualTo(MissionAchievementLevel.HIGH_PROGRESS);
        assertThat(MissionAchievementLevel.from(40.0)).isEqualTo(MissionAchievementLevel.MID_PROGRESS);
        assertThat(MissionAchievementLevel.from(39.9)).isEqualTo(MissionAchievementLevel.LOW_PROGRESS);
    }
}
