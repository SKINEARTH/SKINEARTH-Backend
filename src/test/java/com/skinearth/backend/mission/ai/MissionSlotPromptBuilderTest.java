package com.skinearth.backend.mission.ai;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MissionSlotPromptBuilderTest {

    private final MissionSlotPromptBuilder promptBuilder = new MissionSlotPromptBuilder();

    @Test
    void buildsPromptForShortActionGuidanceWithoutMentioningThePrimaryCause() {
        MissionTemplate template = MissionTemplate.builder()
                .cause("수면 부족")
                .category("숙면 준비")
                .actionType("취침 전 스트레칭")
                .intensity("가벼운")
                .timing("취침전")
                .isActive(true)
                .build();

        String prompt = promptBuilder.build(List.of(template));

        assertThat(prompt)
                .contains("행동을 안내하는 한 문장 (25~55자)")
                .contains("주요 원인, 추천 이유, 카테고리 이름을 언급하지 말 것")
                .contains("0. 행동유형: 취침 전 스트레칭")
                .doesNotContain("오늘의 주요 원인")
                .doesNotContain("\"title\"");
    }
}
