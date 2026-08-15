package com.skinearth.backend.mission.ai;

import com.skinearth.backend.forecast.ai.GeminiClient;
import com.skinearth.backend.mission.entity.MissionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionCardGeneratorTest {

    @Mock
    private MissionSlotSelector slotSelector;
    @Mock
    private MissionSlotPromptBuilder promptBuilder;
    @Mock
    private GeminiClient geminiClient;
    @Mock
    private ForbiddenWordFilter forbiddenWordFilter;

    private MissionCardGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new MissionCardGenerator(slotSelector, promptBuilder, geminiClient, forbiddenWordFilter);
    }

    @Test
    void usesTheTemplateDisplayTitleInsteadOfAnAiGeneratedTitle() {
        MissionTemplate template = MissionTemplate.builder()
                .cause("식사규칙성")
                .category("규칙적 식사")
                .actionType("식사 알림 설정")
                .displayTitle("지금 가볍게 식사 알림 설정하기")
                .intensity("가벼운")
                .timing("지금")
                .isActive(true)
                .build();
        when(slotSelector.findEasyCandidates("식사규칙성", "식사 알림 설정"))
                .thenReturn(List.of(template));
        when(promptBuilder.build(List.of(template))).thenReturn("prompt");
        when(geminiClient.generateComment("prompt"))
                .thenReturn("{\"selectedIndex\":0,\"title\":\"AI 제목\",\"description\":\"식사 시간을 놓치지 않도록 알림을 설정해 보세요.\"}");
        when(forbiddenWordFilter.containsForbiddenWord("식사 시간을 놓치지 않도록 알림을 설정해 보세요."))
                .thenReturn(false);

        MissionCardGenerator.MissionSlotResult result = generator.generateWithFixedActionType(
                "식사규칙성", "식사 알림 설정"
        );

        assertThat(result.title()).isEqualTo("지금 가볍게 식사 알림 설정하기");
    }
}
