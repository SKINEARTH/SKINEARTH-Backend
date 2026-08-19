package com.skinearth.backend.forecast.ai;

import org.springframework.stereotype.Component;

@Component
public class ForecastCommentPromptBuilder {

    public String build(String nickname, int riskScore, String riskLevel,
                        String factor1Name, String factor1Level,
                        String factor2Name, String factor2Level) {
        StringBuilder factorDescription = new StringBuilder();
        if (factor1Name != null) {
            factorDescription.append(factor1Name).append("(위험 기여도 ").append(factor1Level).append(")");
        }
        if (factor2Name != null) {
            factorDescription.append(", ").append(factor2Name).append("(위험 기여도 ").append(factor2Level).append(")");
        }
        if (factorDescription.isEmpty()) {
            factorDescription.append("뚜렷한 원인 없음");
        }

        return """
                너는 피부 상태 예보 서비스의 AI 코멘트 작성자야.
                아래 정보를 참고해서, 사용자에게 보여줄 2~3문장, 공백 포함 100자 이내의 자연스러운 한국어 코멘트를 작성해줘.

                [내일 예측 정보]
                - 위험도 점수: %d (등급: %s)
                - 주요 원인: %s
                - 사용자 닉네임: %s (자연스럽게 활용 가능, 필수는 아님)

                [용어 설명]
                - "위험 기여도"는 실제 습관 수치가 아니라, 그 요인이 피부 위험도에 미치는 영향 수준이다.
                - 위험 기여도 "낮음"은 그 요인이 안정적이고 양호한 습관이라는 뜻이며, "높음"은 개선이 필요하다는 뜻이다. 절대 반대로 해석하지 말 것.

                [작성 규칙]
                - 3문장을 넘기지 말고, 공백을 포함해 100자 이내로 간결하게 작성
                - 반드시 "주요 원인 언급 → 예상되는 영향 → 구체적인 행동 제안" 순서로 작성
                - 위험 기여도가 낮은 요인은 안정적으로 잘 관리되고 있다고 서술하고, 높은 요인만 개선이 필요하다고 서술할 것
                - 의학적 진단, 치료, "낫는다", "완치" 등의 표현은 절대 사용하지 말 것
                - 과장되거나 겁을 주는 표현 대신, 담백하고 친근한 톤 유지
                - 코멘트 텍스트만 출력하고, 다른 설명이나 따옴표는 붙이지 말 것
                """.formatted(riskScore, riskLevel, factorDescription, nickname != null ? nickname : "회원");
    }
}
