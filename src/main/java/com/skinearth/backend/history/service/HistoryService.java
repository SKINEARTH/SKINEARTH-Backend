package com.skinearth.backend.history.service;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.history.dto.CauseTimelineItemResponse;
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
import java.util.ArrayList;
import java.util.Collections;
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

    @Transactional(readOnly = true)
    public List<CauseTimelineItemResponse> getCauseTimeline(Long userId, HistoryPeriod period) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = startDate(period, today);
        LocalDate endDate = endDate(period, today);
        List<DailyRecord> records = dailyRecordRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, startDate, endDate);

        List<CauseTimelineItemResponse> timeline = new ArrayList<>();
        for (DailyRecord record : records) {
            CauseCandidate cause = primaryCause(record);
            if (cause == null) {
                continue;
            }
            String level = timelineLevel(cause.riskScore());

            if (!timeline.isEmpty()) {
                CauseTimelineItemResponse previous = timeline.get(timeline.size() - 1);
                if (previous.factorName().equals(cause.factorName())
                        && previous.level().equals(level)
                        && previous.endDate().plusDays(1).equals(record.getRecordDate())) {
                    timeline.set(timeline.size() - 1, new CauseTimelineItemResponse(
                            previous.startDate(), record.getRecordDate(), cause.factorName(), level
                    ));
                    continue;
                }
            }

            timeline.add(new CauseTimelineItemResponse(
                    record.getRecordDate(), record.getRecordDate(), cause.factorName(), level
            ));
        }

        Collections.reverse(timeline);
        return List.copyOf(timeline);
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

    private CauseCandidate primaryCause(DailyRecord record) {
        List<CauseCandidate> candidates = List.of(
                new CauseCandidate("에어컨 노출", normalizeDirect(record.getAcLevel())),
                new CauseCandidate("스크린 노출", normalizeDirect(record.getScreenTime())),
                new CauseCandidate("수면 부족", normalizeSleep(record.getSleepHours())),
                new CauseCandidate("스트레스", normalizeDirect(record.getStressLevel())),
                new CauseCandidate("식사 불규칙", normalizeMeal(record.getMealRegularity()))
        );

        CauseCandidate selected = null;
        for (CauseCandidate candidate : candidates) {
            if (candidate.riskScore() != null
                    && (selected == null || candidate.riskScore() > selected.riskScore())) {
                selected = candidate;
            }
        }
        return selected;
    }

    private Double normalizeDirect(Integer value) {
        return value == null ? null : (value - 1) / 4.0 * 100.0;
    }

    private Double normalizeMeal(Integer value) {
        return value == null ? null : (5 - value) / 4.0 * 100.0;
    }

    private Double normalizeSleep(Integer hours) {
        return hours == null ? null : hours < 7 ? (7 - hours) / 7.0 * 100.0 : 0.0;
    }

    private String timelineLevel(double riskScore) {
        if (riskScore <= 39) return "안정";
        if (riskScore <= 69) return "주의";
        return "위험";
    }

    private record CauseCandidate(String factorName, Double riskScore) {
    }
}
