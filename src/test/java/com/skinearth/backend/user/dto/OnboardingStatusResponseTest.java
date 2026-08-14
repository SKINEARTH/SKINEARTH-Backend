package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OnboardingStatusResponseTest {

    @Test
    void returnsFirstRecordGuideStateBeforeAnyRecordExists() {
        OnboardingStatusResponse response = OnboardingStatusResponse.of(personalizedUser(), 0, 10);

        assertThat(response.nickname()).isEqualTo("박수현");
        assertThat(response.personalizationCompleted()).isTrue();
        assertThat(response.validRecordCount()).isZero();
        assertThat(response.targetRecordCount()).isEqualTo(10);
        assertThat(response.remainingRecordCount()).isEqualTo(10);
        assertThat(response.firstRecordCompleted()).isFalse();
        assertThat(response.forecastReady()).isFalse();
    }

    @Test
    void neverReturnsNegativeRemainingCount() {
        OnboardingStatusResponse response = OnboardingStatusResponse.of(personalizedUser(), 12, 10);

        assertThat(response.remainingRecordCount()).isZero();
        assertThat(response.firstRecordCompleted()).isTrue();
        assertThat(response.forecastReady()).isTrue();
    }

    private User personalizedUser() {
        return User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("박수현")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }
}
