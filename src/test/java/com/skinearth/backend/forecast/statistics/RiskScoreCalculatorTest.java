package com.skinearth.backend.forecast.statistics;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RiskScoreCalculatorTest {

    private final RiskScoreCalculator calculator = new RiskScoreCalculator();

    @Test
    void 완벽한_양의_상관관계면_상관계수는_1이다() {
        // given
        List<Double> x = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> y = List.of(1.0, 2.0, 3.0, 4.0, 5.0);

        // when
        double result = calculator.calculatePearsonCorrelation(x, y);

        // then
        assertThat(result).isCloseTo(1.0, within(0.001));
    }

    @Test
    void 완벽한_음의_상관관계면_상관계수는_마이너스1이다() {
        List<Double> x = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> y = List.of(5.0, 4.0, 3.0, 2.0, 1.0);

        double result = calculator.calculatePearsonCorrelation(x, y);

        assertThat(result).isCloseTo(-1.0, within(0.001));
    }

    @Test
    void 모든_값이_같으면_0을_반환한다() {
        List<Double> x = List.of(3.0, 3.0, 3.0, 3.0);
        List<Double> y = List.of(1.0, 2.0, 3.0, 4.0);

        double result = calculator.calculatePearsonCorrelation(x, y);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void 후보가_2개_이상이면_상위_2개만_반환한다() {
        // given
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("스크린타임", -0.8, 0.0),
                new FactorCorrelation("수면시간", 0.7, 0.0),
                new FactorCorrelation("스트레스", -0.5, 0.0),
                new FactorCorrelation("냉난방", 0.1, 0.0),      // 0.2 미만이라 애초에 후보 아님
                new FactorCorrelation("식사규칙성", -0.05, 0.0)  // 0.2 미만
        );

        // when
        List<FactorCorrelation> result = calculator.selectPrimaryFactors(factors);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).variableName()).isEqualTo("스크린타임"); // 절댓값 0.8로 1위
        assertThat(result.get(1).variableName()).isEqualTo("수면시간");   // 절댓값 0.7로 2위
    }

    @Test
    void 후보가_1개면_1개만_반환한다() {
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("스크린타임", -0.5, 0.0),
                new FactorCorrelation("수면시간", 0.1, 0.0),
                new FactorCorrelation("스트레스", -0.05, 0.0)
        );

        List<FactorCorrelation> result = calculator.selectPrimaryFactors(factors);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).variableName()).isEqualTo("스크린타임");
    }

    @Test
    void 후보가_0개면_빈_리스트를_반환한다() {
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("스크린타임", 0.1, 0.0),
                new FactorCorrelation("수면시간", -0.05, 0.0),
                new FactorCorrelation("스트레스", 0.0, 0.0)
        );

        List<FactorCorrelation> result = calculator.selectPrimaryFactors(factors);

        assertThat(result).isEmpty();
    }

    @Test
    void 원인이_불명확하면_위험도는_50이다() {
        List<FactorCorrelation> factors = List.of();

        double result = calculator.calculateRiskScore(factors);

        assertThat(result).isEqualTo(50.0);
    }

    @Test
    void 양의_상관관계면_정규화값을_뒤집어서_반영한다() {
        // given: 원인 1개, 양의 상관관계(보호요인), 예측값 정규화 80
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("수면시간", 0.7, 80.0)
        );

        // when
        double result = calculator.calculateRiskScore(factors);

        // then: 보호요인이라 뒤집어야 함 → 100-80=20
        assertThat(result).isCloseTo(20.0, within(0.001));
    }

    @Test
    void 음의_상관관계면_정규화값을_그대로_반영한다() {
        // given: 원인 1개, 음의 상관관계(위험요인), 예측값 정규화 80
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("스크린타임", -0.7, 80.0)
        );

        // when
        double result = calculator.calculateRiskScore(factors);

        // then: 위험요인이라 그대로 → 80
        assertThat(result).isCloseTo(80.0, within(0.001));
    }

    @Test
    void 두_원인의_가중평균을_계산한다() {
        // given
        // 스크린타임: r=-0.8(위험요인), 정규화값=90 → 보정값 = 90 (그대로)
        // 수면시간:   r=0.6(보호요인),  정규화값=30 → 보정값 = 100-30 = 70
        // 예상 = (0.8*90 + 0.6*70) / (0.8+0.6) = (72+42)/1.4 = 81.43
        List<FactorCorrelation> factors = List.of(
                new FactorCorrelation("스크린타임", -0.8, 90.0),
                new FactorCorrelation("수면시간", 0.6, 30.0)
        );

        double result = calculator.calculateRiskScore(factors);

        assertThat(result).isCloseTo(81.43, within(0.01));
    }

    @Test
    void 등급_낮음은_39이하다() {
        assertThat(calculator.determineRiskLevel(39)).isEqualTo("낮음");
    }

    @Test
    void 등급_보통은_40에서_69다() {
        assertThat(calculator.determineRiskLevel(40)).isEqualTo("보통");
        assertThat(calculator.determineRiskLevel(69)).isEqualTo("보통");
    }

    @Test
    void 등급_높음은_70이상이다() {
        assertThat(calculator.determineRiskLevel(70)).isEqualTo("높음");
    }
}