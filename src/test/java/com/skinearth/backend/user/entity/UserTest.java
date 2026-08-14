package com.skinearth.backend.user.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createsUserWithInitialStageOne() {
        User user = validUser();

        assertThat(user.getStage()).isOne();
    }

    @Test
    void requiresServiceTermsAgreement() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("스킨어스")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS))
                .serviceTermsAgreed(false)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();

        assertThat(validator.validate(user))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("serviceTermsAgreed"));
    }

    @Test
    void allowsResearchDataAgreementToBeDeclined() {
        User user = validUser();

        assertThat(validator.validate(user)).isEmpty();
        assertThat(user.isResearchDataAgreed()).isFalse();
    }

    @Test
    void resetsServiceDataWhileKeepingAccountAndAgreements() {
        User user = validUser();

        user.resetServiceData();

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getNickname()).isNull();
        assertThat(user.getUserStatus()).isNull();
        assertThat(user.getSkinConcerns()).isEmpty();
        assertThat(user.isPersonalizationCompleted()).isFalse();
        assertThat(user.getStage()).isOne();
        assertThat(user.isServiceTermsAgreed()).isTrue();
        assertThat(user.isSensitiveDataAgreed()).isTrue();
    }

    private User validUser() {
        return User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("스킨어스")
                .userStatus(UserStatus.STUDENT)
                .skinConcerns(List.of(SkinConcern.TROUBLE))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }
}
