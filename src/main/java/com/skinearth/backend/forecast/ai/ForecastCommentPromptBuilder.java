package com.skinearth.backend.forecast.ai;

import org.springframework.stereotype.Component;

@Component
public class ForecastCommentPromptBuilder {

    public String build(String nickname, int riskScore, String riskLevel,
                        String factor1Name, String factor1Level,
                        String factor2Name, String factor2Level) {
        StringBuilder factorDescription = new StringBuilder();
        if (factor1Name != null) {
            factorDescription.append(factor1Name).append("(").append(factor1Level).append(")");
        }
        if (factor2Name != null) {
            factorDescription.append(", ").append(factor2Name).append("(").append(factor2Level).append(")");
        }
        if (factorDescription.isEmpty()) {
            factorDescription.append("뚜렷한 원인 없음");
        }

        return """
                너는 피부 상태 예보 서비스의 AI 코멘트 작성자야.
                아래 정보를 참고해서, 사용자에게 보여줄 2~3문장의 자연스러운 한국어 코멘트를 작성해줘.

                [내일 예측 정보]
                - 위험도 점수: %d (등급: %s)
                - 주요 원인: %s
                - 사용자 닉네임: %s (자연스럽게 활용 가능, 필수는 아님)

                [작성 규칙]
                - 반드시 "주요 원인 언급 → 예상되는 영향 → 구체적인 행동 제안" 순서로 작성
                - 의학적 진단, 치료, "낫는다", "완치" 등의 표현은 절대 사용하지 말 것
                - 과장되거나 겁을 주는 표현 대신, 담백하고 친근한 톤 유지
                - 코멘트 텍스트만 출력하고, 다른 설명이나 따옴표는 붙이지 말 것
                """.formatted(riskScore, riskLevel, factorDescription, nickname != null ? nickname : "회원");
    }
}