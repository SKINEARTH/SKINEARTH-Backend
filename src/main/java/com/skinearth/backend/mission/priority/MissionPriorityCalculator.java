package com.skinearth.backend.mission.priority;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class MissionPriorityCalculator {

    public enum MissionCategory {
        HYDRATION,        // 수분 보충 (냉난방)
        INDOOR_CARE,       // 실내환경/자극 관리 (스크린타임)
        SLEEP_PREP,        // 숙면 준비 (수면)
        STRESS_RELIEF,     // 긴장 완화 (스트레스)
        MEAL_REGULARITY    // 규칙적 식사 (식사규칙성)
    }

    private static final Map<UserStatus, Map<MissionCategory, Integer>> STATUS_BASE_SCORE = Map.of(
            UserStatus.EMPLOYEE, Map.of(
                    MissionCategory.HYDRATION, 5,
                    MissionCategory.STRESS_RELIEF, 4,
                    MissionCategory.INDOOR_CARE, 3,
                    MissionCategory.SLEEP_PREP, 2,
                    MissionCategory.MEAL_REGULARITY, 1
            ),
            UserStatus.STUDENT, Map.of(
                    MissionCategory.SLEEP_PREP, 5,
                    MissionCategory.MEAL_REGULARITY, 4,
                    MissionCategory.STRESS_RELIEF, 3,
                    MissionCategory.INDOOR_CARE, 2,
                    MissionCategory.HYDRATION, 1
            ),
            UserStatus.OTHER, Map.of(
                    MissionCategory.HYDRATION, 3,
                    MissionCategory.INDOOR_CARE, 3,
                    MissionCategory.SLEEP_PREP, 3,
                    MissionCategory.STRESS_RELIEF, 3,
                    MissionCategory.MEAL_REGULARITY, 3
            )
    );

    private static final Map<SkinConcern, Set<MissionCategory>> CONCERN_CATEGORY_MAP = Map.of(
            SkinConcern.DRYNESS, Set.of(MissionCategory.HYDRATION),
            SkinConcern.SENSITIVITY, Set.of(MissionCategory.HYDRATION, MissionCategory.STRESS_RELIEF),
            SkinConcern.TROUBLE, Set.of(MissionCategory.STRESS_RELIEF, MissionCategory.MEAL_REGULARITY, MissionCategory.SLEEP_PREP),
            SkinConcern.DULLNESS, Set.of(MissionCategory.SLEEP_PREP),
            SkinConcern.PORES, Set.of(MissionCategory.STRESS_RELIEF, MissionCategory.HYDRATION),
            SkinConcern.OILINESS, Set.of(MissionCategory.STRESS_RELIEF, MissionCategory.MEAL_REGULARITY)
    );

    public Map<MissionCategory, Integer> calculate(UserStatus status, Set<SkinConcern> concerns) {
        Map<MissionCategory, Integer> scores = new EnumMap<>(STATUS_BASE_SCORE.get(status));

        for (SkinConcern concern : concerns) {
            Set<MissionCategory> relatedCategories = CONCERN_CATEGORY_MAP.getOrDefault(concern, Set.of());
            for (MissionCategory category : relatedCategories) {
                scores.merge(category, 1, Integer::sum);
            }
        }

        return scores;
    }
}