package com.skinearth.backend.mission.priority;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MissionPriorityCalculatorTest {

    private final MissionPriorityCalculator calculator = new MissionPriorityCalculator();

    @Test
    void 직장인_기본점수만_적용된다() {
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.EMPLOYEE, Set.of());

        assertThat(result.get(MissionPriorityCalculator.MissionCategory.HYDRATION)).isEqualTo(5);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.STRESS_RELIEF)).isEqualTo(4);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY)).isEqualTo(1);
    }

    @Test
    void 학생_기본점수만_적용된다() {
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.STUDENT, Set.of());

        assertThat(result.get(MissionPriorityCalculator.MissionCategory.SLEEP_PREP)).isEqualTo(5);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.HYDRATION)).isEqualTo(1);
    }

    @Test
    void 기타_상태는_모든_카테고리가_균등하다() {
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.OTHER, Set.of());

        assertThat(result.values()).allMatch(score -> score == 3);
    }

    @Test
    void 피부고민_하나_추가시_해당_카테고리에_1점_가산된다() {
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.EMPLOYEE, Set.of(SkinConcern.DRYNESS));

        // 기본 5점(HYDRATION) + 건조함 보너스 1점 = 6점
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.HYDRATION)).isEqualTo(6);
    }

    @Test
    void 여러_카테고리에_걸친_피부고민이_각각_가산된다() {
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.EMPLOYEE, Set.of(SkinConcern.SENSITIVITY));

        // 민감성 → HYDRATION, STRESS_RELIEF 둘 다 +1
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.HYDRATION)).isEqualTo(6);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.STRESS_RELIEF)).isEqualTo(5);
    }

    @Test
    void 여러_피부고민이_같은_카테고리에_중복_가산된다() {
        // 민감성(STRESS_RELIEF+1), 트러블(STRESS_RELIEF+1) → STRESS_RELIEF +2
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.EMPLOYEE,
                        Set.of(SkinConcern.SENSITIVITY, SkinConcern.TROUBLE));

        assertThat(result.get(MissionPriorityCalculator.MissionCategory.STRESS_RELIEF)).isEqualTo(6); // 4+1+1
    }

    @Test
    void 여러_피부고민_선택시_최종_점수가_합산된다() {
        // 직장인 + 트러블, 모공 (Image 3 예시와 동일 시나리오)
        Map<MissionPriorityCalculator.MissionCategory, Integer> result =
                calculator.calculate(UserStatus.EMPLOYEE,
                        Set.of(SkinConcern.TROUBLE, SkinConcern.PORES));

        // HYDRATION: 5(기본) + 1(모공) = 6
        // STRESS_RELIEF: 4(기본) + 1(트러블) + 1(모공) = 6
        // MEAL_REGULARITY: 1(기본) + 1(트러블) = 2
        // SLEEP_PREP: 2(기본) + 1(트러블) = 3
        // INDOOR_CARE: 3(기본)
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.HYDRATION)).isEqualTo(6);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.STRESS_RELIEF)).isEqualTo(6);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.MEAL_REGULARITY)).isEqualTo(2);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.SLEEP_PREP)).isEqualTo(3);
        assertThat(result.get(MissionPriorityCalculator.MissionCategory.INDOOR_CARE)).isEqualTo(3);
    }
}