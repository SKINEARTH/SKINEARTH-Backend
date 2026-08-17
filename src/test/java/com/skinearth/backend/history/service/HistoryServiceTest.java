package com.skinearth.backend.history.service;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.history.dto.CauseTimelineItemResponse;
import com.skinearth.backend.history.dto.HistoryPeriod;
import com.skinearth.backend.history.dto.HistoryResponse;
import com.skinearth.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    private static final long USER_ID = 1L;

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    private HistoryService historyService;
    private User user;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        historyService = new HistoryService(dailyRecordRepository, clock);
        user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }

    @Test
    void returnsWeeklySummaryAndIncludesDatesWithoutRecords() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 16);
        when(dailyRecordRepository.findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of(record(startDate, 3), record(startDate.plusDays(2), 4)));

        HistoryResponse response = historyService.get(
                USER_ID, HistoryPeriod.WEEKLY, LocalDate.of(2026, 8, 14));

        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.recordCount()).isEqualTo(2);
        assertThat(response.averageSkinCondition()).isEqualTo(3.5);
        assertThat(response.points()).hasSize(7);
        assertThat(response.points().get(1).skinCondition()).isNull();
    }

    @Test
    void returnsWholeMonthAndNullAverageWhenNoRecordsExist() {
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 28);
        when(dailyRecordRepository.findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of());

        HistoryResponse response = historyService.get(
                USER_ID, HistoryPeriod.MONTHLY, LocalDate.of(2026, 2, 10));

        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.recordCount()).isZero();
        assertThat(response.averageSkinCondition()).isNull();
        assertThat(response.points()).hasSize(28)
                .allMatch(point -> point.skinCondition() == null);
    }

    @Test
    void usesTodayWhenAnchorDateIsOmitted() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 16);
        when(dailyRecordRepository.findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of());

        HistoryResponse response = historyService.get(USER_ID, HistoryPeriod.WEEKLY, null);

        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
    }

    @Test
    void groupsConsecutiveSameFactorsAndReturnsLatestFirst() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 16);
        when(dailyRecordRepository.findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of(
                record(LocalDate.of(2026, 8, 10), null, null, 8, null, null),
                record(LocalDate.of(2026, 8, 11), null, null, 8, null, null),
                record(LocalDate.of(2026, 8, 12), null, null, null, 3, null),
                record(LocalDate.of(2026, 8, 13), null, null, null, 3, null),
                record(LocalDate.of(2026, 8, 14), 5, null, null, null, null),
                record(LocalDate.of(2026, 8, 15), 5, null, null, null, null),
                record(LocalDate.of(2026, 8, 16), 3, null, null, null, null)
        ));

        List<CauseTimelineItemResponse> response = historyService.getCauseTimeline(USER_ID, HistoryPeriod.WEEKLY);

        assertThat(response).containsExactly(
                new CauseTimelineItemResponse(
                        LocalDate.of(2026, 8, 16), LocalDate.of(2026, 8, 16), "에어컨 노출", "주의"
                ),
                new CauseTimelineItemResponse(
                        LocalDate.of(2026, 8, 14), LocalDate.of(2026, 8, 15), "에어컨 노출", "위험"
                ),
                new CauseTimelineItemResponse(
                        LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 13), "스트레스", "주의"
                ),
                new CauseTimelineItemResponse(
                        LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 11), "수면 부족", "안정"
                )
        );
    }

    private DailyRecord record(LocalDate date, int skinCondition) {
        return DailyRecord.builder()
                .user(user)
                .recordDate(date)
                .skinCondition(skinCondition)
                .build();
    }

    private DailyRecord record(LocalDate date, Integer acLevel, Integer screenTime, Integer sleepHours,
                               Integer stressLevel, Integer mealRegularity) {
        return DailyRecord.builder()
                .user(user)
                .recordDate(date)
                .acLevel(acLevel)
                .screenTime(screenTime)
                .sleepHours(sleepHours)
                .stressLevel(stressLevel)
                .mealRegularity(mealRegularity)
                .skinCondition(3)
                .build();
    }
}
