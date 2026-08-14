package com.skinearth.backend.history.service;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.history.dto.HistoryPeriod;
import com.skinearth.backend.history.dto.HistoryPointResponse;
import com.skinearth.backend.history.dto.HistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final DailyRecordRepository dailyRecordRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public HistoryResponse get(Long userId, HistoryPeriod period, LocalDate anchorDate) {
        LocalDate effectiveDate = anchorDate == null ? LocalDate.now(clock) : anchorDate;
        LocalDate startDate = startDate(period, effectiveDate);
        LocalDate endDate = endDate(period, effectiveDate);

        List<DailyRecord> records = dailyRecordRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, startDate, endDate);
        Map<LocalDate, DailyRecord> recordsByDate = records.stream()
                .collect(Collectors.toMap(DailyRecord::getRecordDate, Function.identity()));

        List<HistoryPointResponse> points = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    DailyRecord record = recordsByDate.get(date);
                    return new HistoryPointResponse(date, record == null ? null : record.getSkinCondition());
                })
                .toList();

        Double average = records.isEmpty()
                ? null
                : Math.round(records.stream()
                        .mapToInt(DailyRecord::getSkinCondition)
                        .average()
                        .orElseThrow() * 10.0) / 10.0;

        return new HistoryResponse(period, startDate, endDate, records.size(), average, points);
    }

    private LocalDate startDate(HistoryPeriod period, LocalDate anchorDate) {
        return switch (period) {
            case WEEKLY -> anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> anchorDate.withDayOfMonth(1);
        };
    }

    private LocalDate endDate(HistoryPeriod period, LocalDate anchorDate) {
        return switch (period) {
            case WEEKLY -> anchorDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTHLY -> anchorDate.with(TemporalAdjusters.lastDayOfMonth());
        };
    }
}
