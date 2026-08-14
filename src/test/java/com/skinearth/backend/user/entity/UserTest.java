package com.skinearth.backend.user.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createsUserWithInitialStageZero() {
        User user = validUser();

        assertThat(user.getStage()).isZero();
    }

    @Test
    void requiresServiceTermsAgreement() {
        User user = User.builder()
                .nickname("스킨어스")
                .userStatus(UserStatus.EMPLOYEE)
                .skinType(SkinType.DRY)
                .skinConcern("건조함")
                .serviceTermsAgreed(false)
                .sensitiveDataAgreed(true)
                .thirdPartyDataAgreed(false)
                .build();

        assertThat(validator.validate(user))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("serviceTermsAgreed"));
    }

    @Test
    void allowsThirdPartyAgreementToBeDeclined() {
        User user = validUser();

        assertThat(validator.validate(user)).isEmpty();
        assertThat(user.isThirdPartyDataAgreed()).isFalse();
    }

    private User validUser() {
        return User.builder()
                .nickname("스킨어스")
                .userStatus(UserStatus.STUDENT)
                .skinType(SkinType.COMBINATION)
                .skinConcern("트러블")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .thirdPartyDataAgreed(false)
                .build();
    }
}
