package com.skinearth.backend.mission.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinearth.backend.forecast.ai.GeminiClient;
import com.skinearth.backend.mission.entity.MissionCard;
import com.skinearth.backend.mission.entity.MissionTemplate;
import com.skinearth.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class MissionCardGenerator {

    private static final Logger log = LoggerFactory.getLogger(MissionCardGenerator.class);

    private final MissionSlotSelector slotSelector;
    private final MissionSlotPromptBuilder promptBuilder;
    private final GeminiClient geminiClient;
    private final ForbiddenWordFilter forbiddenWordFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MissionCard generate(User user, LocalDate today) {
        String cause = slotSelector.determineTodayCause(user, today);
        return buildCard(user, today, cause, false, false);
    }

    public MissionSlotResult generateAlternative(
            User user,
            LocalDate today,
            String currentCategory,
            Set<String> excludedCategories,
            Set<String> excludedActionTypes
    ) {
        List<MissionTemplate> candidates = slotSelector.findAlternativeCandidates(
                user, today, currentCategory, excludedCategories, excludedActionTypes
        );
        return selectAlternative(candidates);
    }

    public MissionSlotResult generateWithFixedActionType(String cause, String actionType) {
        return selectAlternative(slotSelector.findEasyCandidates(cause, actionType));
    }

    private MissionCard buildCard(User user, LocalDate today, String cause, boolean forceEasy, boolean isReplaced) {
        boolean preferEasy = forceEasy || slotSelector.hasRecentFailure(user.getId(), today);
        List<MissionTemplate> candidates = slotSelector.findCandidates(cause, preferEasy);
        MissionSlotResult result = selectAlternative(candidates);

        return MissionCard.builder()
                .user(user)
                .template(result.template())
                .issuedDate(today)
                .title(result.title())
                .description(result.description())
                .isCompleted(false)
                .isReplaced(isReplaced)
                .build();
    }

    private MissionSlotResult selectAlternative(List<MissionTemplate> candidates) {
        if (candidates.isEmpty()) {
            throw new NoMissionCandidateException();
        }

        MissionSlotResult result = trySelectWithAi(candidates);
        if (result == null) {
            result = fallbackSelect(candidates);
        }
        return result;
    }

    private MissionSlotResult trySelectWithAi(List<MissionTemplate> candidates) {
        try {
            String prompt = promptBuilder.build(candidates);
            String raw = geminiClient.generateComment(prompt);
            String cleaned = raw.replaceAll("```json|```", "").trim();
            MissionSlotSelection selection = objectMapper.readValue(cleaned, MissionSlotSelection.class);

            if (selection.selectedIndex() < 0 || selection.selectedIndex() >= candidates.size()) {
                log.warn("AI가 유효하지 않은 인덱스를 반환했습니다: {}", selection.selectedIndex());
                return null;
            }
            if (selection.description() == null || selection.description().isBlank()
                    || forbiddenWordFilter.containsForbiddenWord(selection.description())) {
                log.warn("AI 응답에 금지어가 포함되어 있습니다.");
                return null;
            }

            MissionTemplate selected = candidates.get(selection.selectedIndex());
            return new MissionSlotResult(selected, selected.getActionType(), selection.description());
        } catch (Exception exception) {
            log.warn("AI 슬롯 선택 실패, 규칙 기반 폴백으로 대체합니다.", exception);
            return null;
        }
    }

    private MissionSlotResult fallbackSelect(List<MissionTemplate> candidates) {
        MissionTemplate template = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        String title = template.getActionType();
        String description = "오늘은 %s 미션을 가볍게 실천해 보세요."
                .formatted(template.getActionType());
        return new MissionSlotResult(template, title, description);
    }

    public record MissionSlotResult(MissionTemplate template, String title, String description) {}

    public static class NoMissionCandidateException extends RuntimeException {
    }
}
