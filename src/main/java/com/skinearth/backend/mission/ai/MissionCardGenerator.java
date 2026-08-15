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
        boolean preferEasy = slotSelector.hasRecentFailure(user.getId(), today);
        List<MissionTemplate> candidates = slotSelector.findCandidates(cause, preferEasy);

        if (candidates.isEmpty()) {
            throw new IllegalStateException("사용 가능한 미션 후보가 없습니다: " + cause);
        }

        MissionSlotResult result = trySelectWithAi(cause, candidates);
        if (result == null) {
            result = fallbackSelect(cause, candidates);
        }

        return MissionCard.builder()
                .user(user)
                .template(result.template())
                .issuedDate(today)
                .title(result.title())
                .description(result.description())
                .isCompleted(false)
                .isReplaced(false)
                .build();
    }

    private MissionSlotResult trySelectWithAi(String cause, List<MissionTemplate> candidates) {
        try {
            String prompt = promptBuilder.build(cause, candidates);
            String raw = geminiClient.generateComment(prompt);
            String cleaned = raw.replaceAll("```json|```", "").trim();
            MissionSlotSelection selection = objectMapper.readValue(cleaned, MissionSlotSelection.class);

            if (selection.selectedIndex() < 0 || selection.selectedIndex() >= candidates.size()) {
                log.warn("AI가 유효하지 않은 인덱스를 반환했습니다: {}", selection.selectedIndex());
                return null;
            }
            if (forbiddenWordFilter.containsForbiddenWord(selection.title())
                    || forbiddenWordFilter.containsForbiddenWord(selection.description())) {
                log.warn("AI 응답에 금지어가 포함되어 있습니다.");
                return null;
            }

            MissionTemplate selected = candidates.get(selection.selectedIndex());
            return new MissionSlotResult(selected, selection.title(), selection.description());
        } catch (Exception exception) {
            log.warn("AI 슬롯 선택 실패, 규칙 기반 폴백으로 대체합니다.", exception);
            return null;
        }
    }

    private MissionSlotResult fallbackSelect(String cause, List<MissionTemplate> candidates) {
        MissionTemplate template = candidates.get(0);
        String title = template.getActionType();
        String description = "%s가 오늘의 주요 원인으로 잡혔어요. %s 미션으로 관리해 보세요."
                .formatted(cause, template.getActionType());
        return new MissionSlotResult(template, title, description);
    }

    private record MissionSlotResult(MissionTemplate template, String title, String description) {}
}