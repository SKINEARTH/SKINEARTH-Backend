package com.skinearth.backend.dailyrecord.dto;

import com.skinearth.backend.dailyrecord.entity.SymptomTag;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRecordRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsRequestWhenEveryEnvironmentFactorIsMissing() {
        DailyRecordRequest request = new DailyRecordRequest(null, null, null, null, null, 3, List.of());

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void acceptsRequestWhenAtLeastOneEnvironmentFactorExists() {
        DailyRecordRequest request = new DailyRecordRequest(null, null, 7, null, null, 3, List.of());

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsOutOfRangeValues() {
        DailyRecordRequest request = new DailyRecordRequest(0, 6, 25, 0, 6, 0, List.of());

        assertThat(validator.validate(request)).hasSize(6);
    }

    @Test
    void requiresSkinCondition() {
        DailyRecordRequest request = new DailyRecordRequest(3, 3, 7, 3, 3, null, List.of());

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void allowsSymptomsToBeEmpty() {
        DailyRecordRequest request = new DailyRecordRequest(3, 3, 7, 3, 3, 3, List.of());

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsDuplicateSymptoms() {
        DailyRecordRequest request = new DailyRecordRequest(
                3, 3, 7, 3, 3, 3,
                List.of(SymptomTag.DRYNESS, SymptomTag.DRYNESS)
        );

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void rejectsNoneWithAnotherSymptom() {
        DailyRecordRequest request = new DailyRecordRequest(
                3, 3, 7, 3, 3, 3,
                List.of(SymptomTag.NONE, SymptomTag.REDNESS)
        );

        assertThat(validator.validate(request)).hasSize(1);
    }
}
