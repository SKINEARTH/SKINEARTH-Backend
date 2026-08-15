package com.skinearth.backend.mission.service;

import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.mission.ai.MissionCardGenerator;
import com.skinearth.backend.mission.ai.PendingMissionCandidateStore;
import com.skinearth.backend.mission.ai.TodayMissionPreferenceStore;
import com.skinearth.backend.mission.dto.MissionAlternativeResponse;
import com.skinearth.backend.mission.dto.MissionCategoryExclusionResponse;
import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.dto.MissionExecutionStatus;
import com.skinearth.backend.mission.dto.MissionHistoryResponse;
import com.skinearth.backend.mission.dto.WeeklyMissionHistoryResponse;
import com.skinearth.backend.mission.entity.MissionCard;
import com.skinearth.backend.mission.exception.MissionActionException;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MissionCardService {

    private final MissionCardRepository missionCardRepository;
    private final UserRepository userRepository;
    private final MissionCardGenerator missionCardGenerator;
    private final MissionStreakCalculator missionStreakCalculator;
    private final PendingMissionCandidateStore pendingStore;
    private final TodayMissionPreferenceStore preferenceStore;
    private final Clock clock;
    private final BadgeService badgeService;

    @Transactional
    public MissionCardResponse getTodayCard(Long userId) {
        LocalDate today = LocalDate.now(clock);
        MissionCard card = missionCardRepository.findByUser_IdAndIssuedDate(userId, today)
                .orElseGet(() -> generateAndSaveTodayCard(userId, today));
        return responseWithStreak(card, userId, today);
    }

    private MissionCard generateAndSaveTodayCard(Long userId, LocalDate today) {
        User user = findUser(userId);
        MissionCard card = missionCardGenerator.generate(user, today);
        return missionCardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public MissionAlternativeResponse regenerate(Long userId) {
        LocalDate today = LocalDate.now(clock);
        User user = findUser(userId);
        MissionCard current = missionCardRepository.findByUser_IdAndIssuedDate(userId, today)
                .orElseThrow(() -> new NotFoundException("오늘 발행된 미션 카드가 없습니다."));

        MissionCardGenerator.MissionSlotResult result = generateAlternative(userId, user, today, current);
        pendingStore.save(userId, new PendingMissionCandidateStore.PendingCandidate(
                result.template(), result.title(), result.description()));

        return new MissionAlternativeResponse(result.title(), result.description(), result.template().getCategory());
    }

    @Transactional(readOnly = true)
    public MissionAlternativeResponse adjustIntensity(Long userId) {
        LocalDate today = LocalDate.now(clock);
        MissionCard current = missionCardRepository.findByUser_IdAndIssuedDate(userId, today)
                .orElseThrow(() -> new NotFoundException("오늘 발행된 미션 카드가 없습니다."));

        if ("가벼운".equals(current.getTemplate().getIntensity())) {
            throw new MissionActionException(
                    "MISSION_ALREADY_LIGHT",
                    "현재 미션은 더 낮은 난이도로 변경할 수 없습니다."
            );
        }

        MissionCardGenerator.MissionSlotResult result;
        try {
            result = missionCardGenerator.generateWithFixedActionType(
                    current.getTemplate().getCause(), current.getTemplate().getActionType()
            );
        } catch (MissionCardGenerator.NoMissionCandidateException exception) {
            throw new MissionActionException(
                    "MISSION_CANNOT_BE_LIGHTENED",
                    "이 미션은 더 가벼운 강도로 조정할 수 없어요."
            );
        }

        pendingStore.save(userId, new PendingMissionCandidateStore.PendingCandidate(
                result.template(), result.title(), result.description()));

        return new MissionAlternativeResponse(result.title(), result.description(), result.template().getCategory());
    }

    @Transactional(readOnly = true)
    public MissionCategoryExclusionResponse excludeCurrentCategory(Long userId) {
        LocalDate today = LocalDate.now(clock);
        MissionCard current = missionCardRepository.findByUser_IdAndIssuedDate(userId, today)
                .orElseThrow(() -> new NotFoundException("오늘 발행된 미션 카드가 없습니다."));

        String category = current.getTemplate().getCategory();
        preferenceStore.excludeCategory(userId, today, category);
        return new MissionCategoryExclusionResponse(category);
    }

    @Transactional
    public MissionCardResponse confirmAlternative(Long userId) {
        LocalDate today = LocalDate.now(clock);
        MissionCard current = missionCardRepository.findByUser_IdAndIssuedDate(userId, today)
                .orElseThrow(() -> new NotFoundException("오늘 발행된 미션 카드가 없습니다."));

        PendingMissionCandidateStore.PendingCandidate candidate = pendingStore.get(userId)
                .orElseThrow(() -> new IllegalStateException("확정할 후보 미션이 없습니다. 먼저 다른 미션을 조회해 주세요."));

        current.updateContent(candidate.template(), candidate.title(), candidate.description());
        pendingStore.clear(userId);
        preferenceStore.clearSeenActionTypes(userId, today);

        return responseWithStreak(missionCardRepository.save(current), userId, today);
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
                startDate, endDate, cards.size(), completedCount, completionRate, cards
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private MissionCardResponse responseWithStreak(MissionCard card, Long userId, LocalDate today) {
        int streak = missionStreakCalculator.calculate(
                missionCardRepository.findCompletedDatesUpTo(userId, today), today
        );
        return MissionCardResponse.from(card, streak);
    }

    private MissionCardGenerator.MissionSlotResult generateAlternative(
            Long userId,
            User user,
            LocalDate today,
            MissionCard current
    ) {
        String currentActionType = current.getTemplate().getActionType();
        Set<String> excludedCategories = preferenceStore.getExcludedCategories(userId, today);
        Set<String> seenActionTypes = preferenceStore.getSeenActionTypes(userId, today);
        Set<String> excludedActionTypes = new java.util.HashSet<>(seenActionTypes);
        excludedActionTypes.add(currentActionType);

        MissionCardGenerator.MissionSlotResult result;
        try {
            result = missionCardGenerator.generateAlternative(
                    user, today, current.getTemplate().getCategory(), excludedCategories, excludedActionTypes
            );
        } catch (MissionCardGenerator.NoMissionCandidateException exception) {
            preferenceStore.clearSeenActionTypes(userId, today);
            try {
                result = missionCardGenerator.generateAlternative(
                        user, today, current.getTemplate().getCategory(), excludedCategories, Set.of(currentActionType)
                );
            } catch (MissionCardGenerator.NoMissionCandidateException ignored) {
                result = missionCardGenerator.generateAlternative(
                        user, today, current.getTemplate().getCategory(), excludedCategories, Set.of()
                );
            }
        }

        preferenceStore.addSeenActionType(userId, today, result.template().getActionType());
        return result;
    }
}
