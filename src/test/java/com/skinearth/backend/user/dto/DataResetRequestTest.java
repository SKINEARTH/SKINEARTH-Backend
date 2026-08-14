package com.skinearth.backend.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataResetRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresExplicitConfirmation() {
        assertThat(validator.validate(new DataResetRequest(false))).hasSize(1);
        assertThat(validator.validate(new DataResetRequest(true))).isEmpty();
    }
}
