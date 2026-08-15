package com.skinearth.backend.forecast.coldstart;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ColdStartCalculator {
    private final ForecastInputNormalizer inputNormalizer;

    public ColdStartResult calculate(UserStatus status, Set<SkinConcern> concerns, ForecastRequestDto request) {
        Map<ForecastFactorType, Integer> scores = baseScores(status);
        Map<ForecastFactorType, Integer> bonuses = new EnumMap<>(ForecastFactorType.class);
        for (ForecastFactorType factor : ForecastFactorType.values()) bonuses.put(factor, 0);
        for (SkinConcern concern : concerns) {
            for (ForecastFactorType factor : relatedFactors(concern)) {
                bonuses.merge(factor, 1, Integer::sum);
                scores.merge(factor, 1, Integer::sum);
            }
        }

        Map<ForecastFactorType, Double> normalized = inputNormalizer.normalize(request);
        List<ForecastFactorType> selected = new ArrayList<>(List.of(ForecastFactorType.values()));
        selected.sort(comparator(scores, bonuses));
        selected = selected.subList(0, Math.min(2, selected.size()));

        double weightedSum = 0;
        int weightSum = 0;
        List<ColdStartFactorResult> factors = new ArrayList<>();
        for (ForecastFactorType factor : selected) {
            int weight = scores.get(factor);
            weightedSum += weight * normalized.get(factor);
            weightSum += weight;
            factors.add(new ColdStartFactorResult(factor, weight, bonuses.get(factor), normalized.get(factor)));
        }
        int riskScore = (int) Math.round(weightedSum / weightSum);
        return new ColdStartResult(riskScore, levelOf(riskScore), List.copyOf(factors));
    }

    private Map<ForecastFactorType, Integer> baseScores(UserStatus status) {
        Map<ForecastFactorType, Integer> scores = new EnumMap<>(ForecastFactorType.class);
        if (status == UserStatus.EMPLOYEE) {
            scores.put(ForecastFactorType.AC, 5); scores.put(ForecastFactorType.STRESS, 4);
            scores.put(ForecastFactorType.SCREEN_TIME, 3); scores.put(ForecastFactorType.SLEEP, 2);
            scores.put(ForecastFactorType.MEAL_REGULARITY, 1);
        } else if (status == UserStatus.STUDENT) {
            scores.put(ForecastFactorType.SLEEP, 5); scores.put(ForecastFactorType.MEAL_REGULARITY, 4);
            scores.put(ForecastFactorType.STRESS, 3); scores.put(ForecastFactorType.SCREEN_TIME, 2);
            scores.put(ForecastFactorType.AC, 1);
        } else {
            for (ForecastFactorType factor : ForecastFactorType.values()) scores.put(factor, 3);
        }
        return scores;
    }

    private List<ForecastFactorType> relatedFactors(SkinConcern concern) {
        return switch (concern) {
            case DRYNESS -> List.of(ForecastFactorType.AC);
            case SENSITIVITY -> List.of(ForecastFactorType.AC, ForecastFactorType.STRESS);
            case TROUBLE -> List.of(ForecastFactorType.STRESS, ForecastFactorType.MEAL_REGULARITY, ForecastFactorType.SLEEP);
            case DULLNESS -> List.of(ForecastFactorType.SLEEP);
            case PORES -> List.of(ForecastFactorType.STRESS, ForecastFactorType.AC);
            case OILINESS -> List.of(ForecastFactorType.STRESS, ForecastFactorType.MEAL_REGULARITY);
        };
    }

    private Comparator<ForecastFactorType> comparator(Map<ForecastFactorType, Integer> scores,
                                                       Map<ForecastFactorType, Integer> bonuses) {
        return Comparator.<ForecastFactorType>comparingInt(scores::get).reversed()
                .thenComparing(Comparator.comparingInt((ForecastFactorType f) -> bonuses.get(f) > 0 ? 1 : 0).reversed())
                .thenComparing(Comparator.comparingInt((ForecastFactorType f) -> bonuses.get(f)).reversed())
                .thenComparingInt(this::bonusTiePriority)
                .thenComparingInt(this::prdPriority);
    }

    private int bonusTiePriority(ForecastFactorType factor) {
        return switch (factor) {
            case STRESS -> 0; case AC -> 1; case SLEEP, MEAL_REGULARITY -> 2; case SCREEN_TIME -> 3;
        };
    }

    private int prdPriority(ForecastFactorType factor) {
        return switch (factor) {
            case AC -> 0; case SCREEN_TIME -> 1; case SLEEP -> 2; case STRESS -> 3; case MEAL_REGULARITY -> 4;
        };
    }

    private String levelOf(int score) {
        if (score <= 39) return "낮음";
        if (score <= 69) return "보통";
        return "높음";
    }
}
