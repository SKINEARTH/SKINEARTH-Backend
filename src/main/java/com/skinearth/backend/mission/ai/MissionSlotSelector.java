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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    private static final Map<String, MissionPriorityCalculator.MissionCategory> CAUSE_TO_CATEGORY = Map.ofEntries(
            Map.entry("냉난방", MissionPriorityCalculator.MissionCategory.HYDRATION),
            Map.entry("냉난방 노출", MissionPriorityCalculator.MissionCategory.HYDRATION),
            Map.entry("스크린타임", MissionPriorityCalculator.MissionCategory.INDOOR_CARE),
            Map.entry("화면 노출", MissionPriorityCalculator.MissionCategory.INDOOR_CARE),
            Map.entry("수면", MissionPriorityCalculator.MissionCategory.SLEEP_PREP),
            Map.entry("수면 시간", MissionPriorityCalculator.MissionCategory.SLEEP_PREP),
            Map.entry("스트레스", MissionPriorityCalculator.MissionCategory.STRESS_RELIEF),
            Map.entry("식사규칙성", MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY),
            Map.entry("식사 규칙성", MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY)
    );

    private final ForecastRepository forecastRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionCardRepository missionCardRepository;
    private final MissionPriorityCalculator priorityCalculator;

    public String determineTodayCause(User user, LocalDate today) {
        Map<MissionPriorityCalculator.MissionCategory, Integer> scores = calculateScores(user, today);

        MissionPriorityCalculator.MissionCategory topCategory = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("더 이상 제안할 수 있는 미션 원인이 없습니다."));

        return CATEGORY_TO_CAUSE.get(topCategory);
    }

    public List<MissionTemplate> findAlternativeCandidates(
            User user,
            LocalDate today,
            String currentCategory,
            Set<String> excludedCategories,
            Set<String> excludedActionTypes
    ) {
        List<MissionTemplate> candidates = calculateScores(user, today).entrySet().stream()
                .sorted(Map.Entry.<MissionPriorityCalculator.MissionCategory, Integer>comparingByValue().reversed())
                .map(entry -> CATEGORY_TO_CAUSE.get(entry.getKey()))
                .flatMap(cause -> missionTemplateRepository.findByCauseAndIsActiveTrue(cause).stream())
                .filter(template -> !excludedCategories.contains(template.getCategory()))
                .filter(template -> !excludedActionTypes.contains(template.getActionType()))
                .toList();

        List<MissionTemplate> differentCategory = candidates.stream()
                .filter(template -> !currentCategory.equals(template.getCategory()))
                .toList();
        List<MissionTemplate> categoryPreferred = differentCategory.isEmpty() ? candidates : differentCategory;

        if (hasRecentFailure(user.getId(), today)) {
            return categoryPreferred.stream()
                    .filter(template -> "가벼운".equals(template.getIntensity()))
                    .toList();
        }

        return selectBalancedIntensity(categoryPreferred);
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

    public List<MissionTemplate> findEasyCandidates(String cause, String actionType) {
        return missionTemplateRepository.findByCauseAndIsActiveTrue(cause).stream()
                .filter(template -> actionType.equals(template.getActionType()))
                .filter(template -> "가벼운".equals(template.getIntensity()))
                .toList();
    }

    public boolean hasRecentFailure(Long userId, LocalDate today) {
        return missionCardRepository.findByUser_IdAndIssuedDate(userId, today.minusDays(1))
                .map(card -> !Boolean.TRUE.equals(card.getIsCompleted()))
                .orElse(false);
    }

    private Map<MissionPriorityCalculator.MissionCategory, Integer> calculateScores(User user, LocalDate today) {
        Map<MissionPriorityCalculator.MissionCategory, Integer> scores =
                priorityCalculator.calculate(user.getUserStatus(), user.getSkinConcerns());

        Optional<Forecast> forecast = forecastRepository.findByUser_IdAndTargetDate(user.getId(), today);
        if (forecast.isPresent() && forecast.get().getPrimaryFactor1Name() != null) {
            MissionPriorityCalculator.MissionCategory primaryCategory =
                    CAUSE_TO_CATEGORY.get(forecast.get().getPrimaryFactor1Name());
            if (primaryCategory != null) {
                scores.merge(primaryCategory, PRIMARY_FACTOR_BONUS, Integer::sum);
            }
        }
        return scores;
    }

    private List<MissionTemplate> selectBalancedIntensity(List<MissionTemplate> candidates) {
        List<String> intensities = candidates.stream()
                .map(MissionTemplate::getIntensity)
                .distinct()
                .toList();
        if (intensities.isEmpty()) {
            return candidates;
        }

        String selectedIntensity = intensities.get(ThreadLocalRandom.current().nextInt(intensities.size()));
        return candidates.stream()
                .filter(template -> selectedIntensity.equals(template.getIntensity()))
                .toList();
    }
}
