package com.skinearth.backend.dailyrecord.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.dto.DailyRecordRequest;
import com.skinearth.backend.dailyrecord.dto.DailyRecordResponse;
import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.entity.SymptomTag;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRecordServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 14);

    @Mock
    private DailyRecordRepository dailyRecordRepository;
    @Mock
    private UserRepository userRepository;

    private DailyRecordService dailyRecordService;
    private User user;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        dailyRecordService = new DailyRecordService(
                dailyRecordRepository,
                userRepository,
                clock,
                new RecordStreakCalculator()
        );
        user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }

    @Test
    void createsTodayRecordForAuthenticatedUser() {
        when(dailyRecordRepository.existsByUserIdAndRecordDate(USER_ID, TODAY)).thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyRecordRepository.saveAndFlush(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dailyRecordRepository.findRecordDatesUpTo(USER_ID, TODAY))
                .thenReturn(List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2)));
        when(dailyRecordRepository.countByUserId(USER_ID)).thenReturn(3L);

        DailyRecordResponse response = dailyRecordService.createToday(USER_ID, request());

        assertThat(response.recordDate()).isEqualTo(TODAY);
        assertThat(response.acLevel()).isEqualTo(3);
        assertThat(response.sleepHours()).isEqualTo(7);
        assertThat(response.skinCondition()).isEqualTo(4);
        assertThat(response.symptoms()).containsExactlyInAnyOrder(SymptomTag.DRYNESS, SymptomTag.REDNESS);
        assertThat(response.currentStreak()).isEqualTo(3);
        assertThat(response.validRecordCount()).isEqualTo(3);
        assertThat(response.targetRecordCount()).isEqualTo(10);
        assertThat(response.forecastReady()).isFalse();
        assertThat(response.forecastTransitionReached()).isFalse();
    }

    @Test
    void rejectsSecondRecordOnSameDay() {
        when(dailyRecordRepository.existsByUserIdAndRecordDate(USER_ID, TODAY)).thenReturn(true);

        assertThatThrownBy(() -> dailyRecordService.createToday(USER_ID, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("오늘의 기록이 이미 존재합니다.");
    }

    @Test
    void updatesOnlyTodayRecord() {
        DailyRecord record = DailyRecord.builder()
                .user(user)
                .recordDate(TODAY)
                .acLevel(1)
                .screenTime(1)
                .sleepHours(5)
                .stressLevel(1)
                .mealRegularity(1)
                .skinCondition(2)
                .build();
        when(dailyRecordRepository.findByUserIdAndRecordDate(USER_ID, TODAY)).thenReturn(Optional.of(record));
        when(dailyRecordRepository.findRecordDatesUpTo(USER_ID, TODAY)).thenReturn(List.of(TODAY));
        when(dailyRecordRepository.countByUserId(USER_ID)).thenReturn(1L);

        DailyRecordResponse response = dailyRecordService.updateToday(USER_ID, request());

        assertThat(response.acLevel()).isEqualTo(3);
        assertThat(response.skinCondition()).isEqualTo(4);
        assertThat(response.currentStreak()).isZero();
        assertThat(response.validRecordCount()).isOne();
    }

    @Test
    void reportsMissingTodayRecord() {
        when(dailyRecordRepository.findByUserIdAndRecordDate(USER_ID, TODAY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyRecordService.getToday(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("오늘의 기록을 찾을 수 없습니다.");
    }

    private DailyRecordRequest request() {
        return new DailyRecordRequest(3, 4, 7, 2, 3, 4,
                List.of(SymptomTag.DRYNESS, SymptomTag.REDNESS));
    }
}
