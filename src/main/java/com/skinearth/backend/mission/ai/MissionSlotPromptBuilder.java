package com.skinearth.backend.mission.ai;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissionSlotPromptBuilder {

    public String build(String primaryCause, List<MissionTemplate> candidates) {
        String candidateList = IntStream.range(0, candidates.size())
                .mapToObj(i -> {
                    MissionTemplate t = candidates.get(i);
                    return "%d. 행동유형: %s, 강도: %s, 타이밍: %s".formatted(
                            i, t.getActionType(), t.getIntensity(), t.getTiming());
                })
                .collect(Collectors.joining("\n"));

        return """
                너는 피부 관리 미션을 추천하는 AI야.
                아래 후보 목록 중 오늘 상황에 가장 적절한 것 하나를 선택하고,
                왜 이 미션이 도움되는지 연결한 짧은 문장을 만들어줘.

                [오늘의 주요 원인]
                %s

                [미션 후보 목록]
                %s

                [출력 형식]
                반드시 아래 JSON 형식으로만 응답해. 다른 설명이나 텍스트는 절대 포함하지 마.
                {
                  "selectedIndex": 선택한 후보의 번호(정수),
                  "title": "미션 제목 (15자 이내, 행동 중심)",
                  "description": "오늘 주요 원인과 연결해서 왜 도움되는지 설명하는 1~2문장"
                }

                [작성 규칙]
                - "%s가 오늘의 주요 원인으로 잡혔어요" 같은 형태로 원인을 자연스럽게 언급할 것
                - 의학적 진단, 치료, "낫는다", "완치" 등의 표현은 절대 사용하지 말 것
                - 친근하고 담백한 톤 유지
                """.formatted(primaryCause, candidateList, primaryCause);
    }
}