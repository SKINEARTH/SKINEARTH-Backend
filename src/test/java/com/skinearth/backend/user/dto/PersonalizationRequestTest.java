package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalizationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidMultipleSkinConcerns() {
        PersonalizationRequest request = new PersonalizationRequest(
                "여행자",
                UserStatus.STUDENT,
                List.of(SkinConcern.DRYNESS, SkinConcern.SENSITIVITY)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void requiresNicknameStatusAndAtLeastOneConcern() {
        PersonalizationRequest request = new PersonalizationRequest(" ", null, List.of());

        assertThat(validator.validate(request)).hasSize(3);
    }

    @Test
    void rejectsDuplicateSkinConcerns() {
        PersonalizationRequest request = new PersonalizationRequest(
                "여행자",
                UserStatus.OTHER,
                List.of(SkinConcern.OILINESS, SkinConcern.OILINESS)
        );

        assertThat(validator.validate(request)).hasSize(1);
    }
}
