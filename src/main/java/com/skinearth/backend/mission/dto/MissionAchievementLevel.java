package com.skinearth.backend.mission.dto;

public enum MissionAchievementLevel {
    HIGH_PROGRESS,
    MID_PROGRESS,
    LOW_PROGRESS;

    public static MissionAchievementLevel from(double completionRatePercent) {
        if (completionRatePercent >= 70.0) {
            return HIGH_PROGRESS;
        }
        if (completionRatePercent >= 40.0) {
            return MID_PROGRESS;
        }
        return LOW_PROGRESS;
    }
}
