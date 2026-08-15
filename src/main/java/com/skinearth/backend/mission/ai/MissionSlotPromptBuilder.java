package com.skinearth.backend.mission.ai;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissionSlotPromptBuilder {

    public String build(List<MissionTemplate> candidates) {
        String candidateList = IntStream.range(0, candidates.size())
                .mapToObj(i -> {
                    MissionTemplate template = candidates.get(i);
                    return "%d. 행동유형: %s, 강도: %s, 타이밍: %s".formatted(
                            i, template.getActionType(), template.getIntensity(), template.getTiming());
                })
                .collect(Collectors.joining("\n"));

        return """
                당신은 피부 관리 미션을 추천하는 AI입니다.
                아래 후보 목록 중 오늘 상황에 가장 적절한 것을 하나 선택하고,
                선택한 행동을 바로 실천할 수 있도록 짧은 안내 문장을 만들어줘.

                [미션 후보 목록]
                %s

                [출력 형식]
                반드시 아래 JSON 형식으로만 응답하고 다른 설명이나 텍스트는 절대 포함하지 마.
                {
                  "selectedIndex": 선택한 후보의 번호(정수),
                  "description": "행동을 안내하는 한 문장 (25~55자)"
                }

                [작성 규칙]
                - 주요 원인, 추천 이유, 카테고리 이름을 언급하지 말 것
                - 선택한 행동과 자연스럽게 연결되는 안내만 작성할 것
                - 의학적 진단, 치료, "낫는다", "완치" 등의 표현은 절대 사용하지 말 것
                - 친근하고 담백한 존댓말로 끝낼 것
                - 질문형 문장, 물음표, "까요", "나요"를 사용하지 말 것
                - 반드시 "~해 보세요." 또는 "~주세요."로 끝낼 것
                """.formatted(candidateList);
    }
}
