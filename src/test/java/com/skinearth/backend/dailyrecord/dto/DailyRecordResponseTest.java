package com.skinearth.backend.dailyrecord.dto;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRecordResponseTest {

    @Test
    void marksForecastTransitionWhenTenthRecordIsReached() {
        DailyRecordResponse response = DailyRecordResponse.from(record(), 4, 10, 10);

        assertThat(response.validRecordCount()).isEqualTo(10);
        assertThat(response.targetRecordCount()).isEqualTo(10);
        assertThat(response.forecastReady()).isTrue();
        assertThat(response.forecastTransitionReached()).isTrue();
    }

    @Test
    void keepsForecastReadyAfterTenthRecordWithoutTransitionFlag() {
        DailyRecordResponse response = DailyRecordResponse.from(record(), 5, 11, 10);

        assertThat(response.forecastReady()).isTrue();
        assertThat(response.forecastTransitionReached()).isFalse();
    }

    private DailyRecord record() {
        return DailyRecord.builder()
                .recordDate(LocalDate.of(2026, 8, 14))
                .acLevel(3)
                .skinCondition(4)
                .symptoms(List.of())
                .build();
    }
}
