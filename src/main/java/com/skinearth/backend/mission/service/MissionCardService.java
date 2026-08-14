package com.skinearth.backend.mission.service;

import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.dto.MissionExecutionStatus;
import com.skinearth.backend.mission.dto.MissionHistoryResponse;
import com.skinearth.backend.mission.dto.WeeklyMissionHistoryResponse;
import com.skinearth.backend.mission.entity.MissionCard;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionCardService {

    private final MissionCardRepository missionCardRepository;
    private final Clock clock;
    private final BadgeService badgeService;

    @Transactional(readOnly = true)
    public MissionCardResponse getTodayCard(Long userId) {
        MissionCard card = missionCardRepository
                .findByUser_IdAndIssuedDate(userId, LocalDate.now(clock))
                .orElseThrow(() -> new NotFoundException("오늘 발행된 미션 카드가 없습니다."));
        return MissionCardResponse.from(card);
    }

    @Transactional
    public MissionHistoryResponse complete(Long userId, Long missionCardId) {
        MissionCard card = missionCardRepository.findByIdAndUser_Id(missionCardId, userId)
                .orElseThrow(() -> new NotFoundException("미션 카드를 찾을 수 없습니다."));
        LocalDate today = LocalDate.now(clock);

        if (!card.getIssuedDate().isEqual(today)) {
            throw new IllegalArgumentException("오늘 발행된 미션만 완료할 수 있습니다.");
        }

        card.complete(LocalDateTime.now(clock));

        badgeService.tryPromote(userId, 0, 0);

        return MissionHistoryResponse.from(card, today);
    }

    @Transactional(readOnly = true)
    public WeeklyMissionHistoryResponse getWeeklyHistory(Long userId, LocalDate anchorDate) {
        LocalDate today = LocalDate.now(clock);
        LocalDate effectiveDate = anchorDate == null ? today : anchorDate;
        LocalDate startDate = effectiveDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = effectiveDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<MissionHistoryResponse> cards = missionCardRepository
                .findAllByUser_IdAndIssuedDateBetweenOrderByIssuedDateDesc(userId, startDate, endDate)
                .stream()
                .map(card -> MissionHistoryResponse.from(card, today))
                .toList();
        int completedCount = (int) cards.stream()
                .filter(card -> card.status() == MissionExecutionStatus.COMPLETED)
                .count();
        double completionRate = cards.isEmpty()
                ? 0.0
                : Math.round(completedCount * 1000.0 / cards.size()) / 10.0;

        return new WeeklyMissionHistoryResponse(
                startDate,
                endDate,
                cards.size(),
                completedCount,
                completionRate,
                cards
        );
    }
}