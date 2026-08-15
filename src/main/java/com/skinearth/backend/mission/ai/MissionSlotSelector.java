package com.skinearth.backend.mission.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final Map<MissionPriorityCalculator.MissionCategory, String> CATEGORY_TO_CAUSE = Map.of(
            MissionPriorityCalculator.MissionCategory.HYDRATION, "냉난방",
            MissionPriorityCalculator.MissionCategory.INDOOR_CARE, "스크린타임",
            MissionPriorityCalculator.MissionCategory.SLEEP_PREP, "수면",
            MissionPriorityCalculator.MissionCategory.STRESS_RELIEF, "스트레스",
            MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY, "식사규칙성"
    );

    private final ForecastRepository forecastRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionCardRepository missionCardRepository;
    private final MissionPriorityCalculator priorityCalculator;

    public String determineTodayCause(User user, LocalDate today) {
        Optional<Forecast> forecast = forecastRepository.findByUser_IdAndTargetDate(user.getId(), today);

        if (forecast.isPresent() && forecast.get().getPrimaryFactor1Name() != null) {
            return forecast.get().getPrimaryFactor1Name();
        }

        // Forecast가 없거나 원인 불명확이면, 5.2 우선순위 1위 카테고리로 대체
        Map<MissionPriorityCalculator.MissionCategory, Integer> scores =
                priorityCalculator.calculate(user.getUserStatus(), user.getSkinConcerns());
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