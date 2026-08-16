package com.skinearth.backend.mission.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.mission.ai.MissionCardGenerator;
import com.skinearth.backend.mission.ai.PendingMissionCandidateStore;
import com.skinearth.backend.mission.ai.TodayMissionPreferenceStore;
import com.skinearth.backend.mission.exception.MissionActionException;
import com.skinearth.backend.mission.dto.MissionExecutionStatus;
import com.skinearth.backend.mission.dto.MissionHistoryResponse;
import com.skinearth.backend.mission.dto.MonthlyMissionCompletionResponse;
import com.skinearth.backend.mission.dto.WeeklyMissionHistoryResponse;
import com.skinearth.backend.mission.entity.MissionCard;
import com.skinearth.backend.mission.entity.MissionTemplate;
import com.skinearth.backend.mission.repository.MissionCardRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MissionCardServiceTest {

    private static final long USER_ID = 1L;
    private static final long CARD_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 14);

    @Mock
    private MissionCardRepository missionCardRepository;
    @Mock
    private BadgeService badgeService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MissionCardGenerator missionCardGenerator;

    private MissionCardService missionCardService;
    private PendingMissionCandidateStore pendingStore;
    private TodayMissionPreferenceStore preferenceStore;
    private User user;
    private MissionTemplate template;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        pendingStore = new PendingMissionCandidateStore();
        preferenceStore = new TodayMissionPreferenceStore();
        missionCardService = new MissionCardService(
                missionCardRepository, userRepository, missionCardGenerator, new MissionStreakCalculator(),
                pendingStore, preferenceStore, clock, badgeService
        );
        user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
        template = MissionTemplate.builder()
                .cause("스트레스")
                .category("긴장 완화")
                .actionType("짧은 산책")
                .intensity("가벼운")
                .timing("지금")
                .isActive(true)
                .build();
    }

    @Test
    void completesTodayMissionAndStoresCompletedAt() {
        MissionCard card = card(TODAY, false, false);
        when(missionCardRepository.findByIdAndUser_Id(CARD_ID, USER_ID))
                .thenReturn(Optional.of(card));

        MissionHistoryResponse response = missionCardService.complete(USER_ID, CARD_ID);

        assertThat(response.status()).isEqualTo(MissionExecutionStatus.COMPLETED);
        assertThat(response.completed()).isTrue();
        assertThat(response.completedAt()).isEqualTo(LocalDateTime.of(2026, 8, 14, 12, 0));
    }

    @Test
    void rejectsMissionOwnedByAnotherUser() {
        when(missionCardRepository.findByIdAndUser_Id(CARD_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> missionCardService.complete(USER_ID, CARD_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("미션 카드를 찾을 수 없습니다.");
    }

    @Test
    void rejectsMissionIssuedOnAnotherDate() {
        MissionCard card = card(TODAY.minusDays(1), false, false);
        when(missionCardRepository.findByIdAndUser_Id(CARD_ID, USER_ID))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() -> missionCardService.complete(USER_ID, CARD_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("오늘 발행된 미션만 완료할 수 있습니다.");
    }

    @Test
    void rejectsDuplicateCompletion() {
        MissionCard card = card(TODAY, false, false);
        card.complete(LocalDateTime.of(2026, 8, 14, 10, 0));
        when(missionCardRepository.findByIdAndUser_Id(CARD_ID, USER_ID))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() -> missionCardService.complete(USER_ID, CARD_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 완료한 미션입니다.");
    }

    @Test
    void calculatesWeeklyCompletionRateAndDelayedFailure() {
        MissionCard completed = card(TODAY.minusDays(4), false, false);
        completed.complete(LocalDateTime.of(2026, 8, 10, 18, 0));
        MissionCard failed = card(TODAY.minusDays(1), false, true);
        MissionCard pending = card(TODAY, false, false);
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 16);
        when(missionCardRepository.findAllByUser_IdAndIssuedDateBetweenOrderByIssuedDateDesc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of(pending, failed, completed));

        WeeklyMissionHistoryResponse response = missionCardService.getWeeklyHistory(USER_ID, TODAY);

        assertThat(response.issuedCount()).isEqualTo(3);
        assertThat(response.completedCount()).isEqualTo(1);
        assertThat(response.completionRatePercent()).isEqualTo(33.3);
        assertThat(response.cards()).extracting(MissionHistoryResponse::status)
                .containsExactly(
                        MissionExecutionStatus.PENDING,
                        MissionExecutionStatus.FAILED,
                        MissionExecutionStatus.COMPLETED
                );
        assertThat(response.cards().get(1).replaced()).isTrue();
    }

    @Test
    void returnsZeroRateWhenNoMissionWasIssued() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 16);
        when(missionCardRepository.findAllByUser_IdAndIssuedDateBetweenOrderByIssuedDateDesc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of());

        WeeklyMissionHistoryResponse response = missionCardService.getWeeklyHistory(USER_ID, null);

        assertThat(response.issuedCount()).isZero();
        assertThat(response.completedCount()).isZero();
        assertThat(response.completionRatePercent()).isZero();
    }

    @Test
    void calculatesMonthlyCompletionRateForCurrentMonth() {
        MissionCard completed = card(LocalDate.of(2026, 8, 3), true, false);
        MissionCard pending = card(LocalDate.of(2026, 8, 14), false, false);
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        when(missionCardRepository.findAllByUser_IdAndIssuedDateBetweenOrderByIssuedDateDesc(
                USER_ID, startDate, endDate
        )).thenReturn(List.of(pending, completed));

        MonthlyMissionCompletionResponse response = missionCardService.getMonthlyHistory(USER_ID);

        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.issuedCount()).isEqualTo(2);
        assertThat(response.completedCount()).isEqualTo(1);
        assertThat(response.completionRatePercent()).isEqualTo(50.0);
    }

    @Test
    void includesStreakWhenTodayMissionIsRetrievedBeforeCompletion() {
        MissionCard current = card(TODAY, false, false);
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));
        when(missionCardRepository.findCompletedDatesUpTo(USER_ID, TODAY)).thenReturn(List.of(
                TODAY.minusDays(1),
                TODAY.minusDays(2)
        ));

        var response = missionCardService.getTodayCard(USER_ID);

        assertThat(response.streak()).isEqualTo(2);
        assertThat(response.estimatedMinutes()).isEqualTo(1);
    }

    @Test
    void includesStreakWhenAlternativeMissionIsConfirmed() {
        MissionCard current = card(TODAY, false, false);
        MissionTemplate alternativeTemplate = template("cause-b", "action-b", "집중");
        pendingStore.save(USER_ID, new PendingMissionCandidateStore.PendingCandidate(
                alternativeTemplate, "alternative", "alternative description"
        ));
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));
        when(missionCardRepository.save(current)).thenReturn(current);
        when(missionCardRepository.findCompletedDatesUpTo(USER_ID, TODAY)).thenReturn(List.of(
                TODAY.minusDays(1),
                TODAY.minusDays(2)
        ));

        var response = missionCardService.confirmAlternative(USER_ID);

        assertThat(response.streak()).isEqualTo(2);
        assertThat(response.title()).isEqualTo("alternative");
    }

    @Test
    void regenerationAvoidsCurrentAndPreviouslySuggestedActionTypes() {
        MissionCard current = card(TODAY, false, false);
        MissionTemplate firstAlternative = template("cause-b", "action-b", "집중");
        MissionTemplate secondAlternative = template("cause-c", "action-c", "집중");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));
        when(missionCardGenerator.generateAlternative(
                eq(user), eq(TODAY), eq(template.getCategory()), anySet(), anySet()
        ))
                .thenReturn(
                        new MissionCardGenerator.MissionSlotResult(firstAlternative, "first", "first description"),
                        new MissionCardGenerator.MissionSlotResult(secondAlternative, "second", "second description")
                );

        var firstResponse = missionCardService.regenerate(USER_ID);
        missionCardService.regenerate(USER_ID);

        assertThat(firstResponse.estimatedMinutes()).isEqualTo(1);
        var actionTypesCaptor = org.mockito.ArgumentCaptor.forClass(Set.class);
        verify(missionCardGenerator, org.mockito.Mockito.times(2))
                .generateAlternative(eq(user), eq(TODAY), eq(template.getCategory()), anySet(), actionTypesCaptor.capture());
        assertThat(actionTypesCaptor.getAllValues().get(0)).containsExactly(template.getActionType());
        assertThat(actionTypesCaptor.getAllValues().get(1))
                .containsExactlyInAnyOrder(template.getActionType(), "action-b");
    }

    @Test
    void adjustingIntensityKeepsTheCurrentCause() {
        MissionTemplate focusedTemplate = template("cause-a", "action-a", "집중");
        MissionCard current = MissionCard.builder()
                .user(user)
                .template(focusedTemplate)
                .issuedDate(TODAY)
                .title("current")
                .description("current description")
                .isCompleted(false)
                .isReplaced(false)
                .build();
        MissionTemplate easyTemplate = template("cause-a", "action-a", "가벼운");
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));
        when(missionCardGenerator.generateWithFixedActionType("cause-a", "action-a"))
                .thenReturn(new MissionCardGenerator.MissionSlotResult(easyTemplate, "easy", "easy description"));

        missionCardService.adjustIntensity(USER_ID);

        verify(missionCardGenerator).generateWithFixedActionType("cause-a", "action-a");
    }

    @Test
    void rejectsIntensityAdjustmentWhenCurrentMissionIsAlreadyLight() {
        MissionCard current = card(TODAY, false, false);
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> missionCardService.adjustIntensity(USER_ID))
                .isInstanceOf(MissionActionException.class)
                .extracting("code")
                .isEqualTo("MISSION_ALREADY_LIGHT");
    }

    @Test
    void excludesCurrentCategoryOnlyForTheCurrentDay() {
        MissionCard current = card(TODAY, false, false);
        when(missionCardRepository.findByUser_IdAndIssuedDate(USER_ID, TODAY)).thenReturn(Optional.of(current));

        var response = missionCardService.excludeCurrentCategory(USER_ID);

        assertThat(response.category()).isEqualTo(template.getCategory());
        assertThat(preferenceStore.getExcludedCategories(USER_ID, TODAY)).containsExactly(template.getCategory());
        assertThat(preferenceStore.getExcludedCategories(USER_ID, TODAY.plusDays(1))).isEmpty();
    }

    private MissionCard card(LocalDate issuedDate, boolean completed, boolean replaced) {
        MissionCard card = MissionCard.builder()
                .user(user)
                .template(template)
                .issuedDate(issuedDate)
                .title("짧은 산책하기")
                .description("잠시 걸으며 긴장을 풀어보세요.")
                .isCompleted(false)
                .isReplaced(replaced)
                .build();
        if (completed) {
            card.complete(issuedDate.atTime(18, 0));
        }
        return card;
    }

    private MissionTemplate template(String cause, String actionType, String intensity) {
        return MissionTemplate.builder()
                .cause(cause)
                .category("category")
                .actionType(actionType)
                .intensity(intensity)
                .timing("now")
                .isActive(true)
                .build();
    }
}
