package com.skinearth.backend.mission.ai;

import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.mission.entity.MissionTemplate;
import com.skinearth.backend.mission.priority.MissionPriorityCalculator;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.mission.repository.MissionTemplateRepository;
import com.skinearth.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MissionSlotSelector {

    private static final int PRIMARY_FACTOR_BONUS = 4;

    private static final Map<MissionPriorityCalculator.MissionCategory, String> CATEGORY_TO_CAUSE = Map.of(
            MissionPriorityCalculator.MissionCategory.HYDRATION, "냉난방",
            MissionPriorityCalculator.MissionCategory.INDOOR_CARE, "스크린타임",
            MissionPriorityCalculator.MissionCategory.SLEEP_PREP, "수면",
            MissionPriorityCalculator.MissionCategory.STRESS_RELIEF, "스트레스",
            MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY, "식사규칙성"
    );

    private static final Map<String, MissionPriorityCalculator.MissionCategory> CAUSE_TO_CATEGORY = Map.of(
            "냉난방", MissionPriorityCalculator.MissionCategory.HYDRATION,
            "스크린타임", MissionPriorityCalculator.MissionCategory.INDOOR_CARE,
            "수면", MissionPriorityCalculator.MissionCategory.SLEEP_PREP,
            "스트레스", MissionPriorityCalculator.MissionCategory.STRESS_RELIEF,
            "식사규칙성", MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY
    );

    private final ForecastRepository forecastRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionCardRepository missionCardRepository;
    private final MissionPriorityCalculator priorityCalculator;

    public String determineTodayCause(User user, LocalDate today) {
        return determineTodayCause(user, today, null);
    }

    public String determineTodayCause(User user, LocalDate today, String excludeCause) {
        Map<MissionPriorityCalculator.MissionCategory, Integer> scores =
                priorityCalculator.calculate(user.getUserStatus(), user.getSkinConcerns());

        Optional<Forecast> forecast = forecastRepository.findByUser_IdAndTargetDate(user.getId(), today);
        if (forecast.isPresent() && forecast.get().getPrimaryFactor1Name() != null) {
            String primaryCause = forecast.get().getPrimaryFactor1Name();
            MissionPriorityCalculator.MissionCategory primaryCategory = CAUSE_TO_CATEGORY.get(primaryCause);
            if (primaryCategory != null) {
                scores.merge(primaryCategory, PRIMARY_FACTOR_BONUS, Integer::sum);
            }
        }

        if (excludeCause != null) {
            MissionPriorityCalculator.MissionCategory excludedCategory = CAUSE_TO_CATEGORY.get(excludeCause);
            if (excludedCategory != null) {
                scores.remove(excludedCategory);
            }
        }

        MissionPriorityCalculator.MissionCategory topCategory = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(MissionPriorityCalculator.MissionCategory.HYDRATION);

        return CATEGORY_TO_CAUSE.get(topCategory);
    }

    public List<MissionTemplate> findCandidates(String cause, boolean preferEasy) {
        List<MissionTemplate> all = missionTemplateRepository.findByCauseAndIsActiveTrue(cause);
        if (preferEasy) {
            List<MissionTemplate> easyOnly = all.stream()
                    .filter(t -> "가벼운".equals(t.getIntensity()))
                    .toList();
            if (!easyOnly.isEmpty()) {
                return easyOnly;
            }
        }
        return all;
    }

    public boolean hasRecentFailure(Long userId, LocalDate today) {
        return missionCardRepository.findByUser_IdAndIssuedDate(userId, today.minusDays(1))
                .map(card -> !Boolean.TRUE.equals(card.getIsCompleted()))
                .orElse(false);
    }
}