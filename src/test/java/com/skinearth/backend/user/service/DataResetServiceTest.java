package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.mission.ai.PendingMissionCandidateStore;
import com.skinearth.backend.mission.ai.TodayMissionPreferenceStore;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import com.skinearth.backend.user.dto.DataResetResponse;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataResetServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private DailyRecordRepository dailyRecordRepository;
    @Mock
    private ForecastRepository forecastRepository;
    @Mock
    private MissionCardRepository missionCardRepository;
    @Mock
    private PendingMissionCandidateStore pendingMissionCandidateStore;
    @Mock
    private TodayMissionPreferenceStore todayMissionPreferenceStore;

    private DataResetService dataResetService;

    @BeforeEach
    void setUp() {
        dataResetService = new DataResetService(
                userRepository,
                dailyRecordRepository,
                forecastRepository,
                missionCardRepository,
                pendingMissionCandidateStore,
                todayMissionPreferenceStore
        );
    }

    @Test
    void resetsAllServiceDataButKeepsAccount() {
        User user = personalizedUser();
        ReflectionTestUtils.setField(user, "stage", 3);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyRecordRepository.deleteByUserId(USER_ID)).thenReturn(12L);
        when(forecastRepository.deleteByUser_Id(USER_ID)).thenReturn(3L);
        when(missionCardRepository.deleteByUser_Id(USER_ID)).thenReturn(5L);

        DataResetResponse response = dataResetService.reset(USER_ID);

        assertThat(response.deletedDailyRecordCount()).isEqualTo(12);
        assertThat(response.deletedForecastCount()).isEqualTo(3);
        assertThat(response.deletedMissionCardCount()).isEqualTo(5);
        assertThat(response.personalizationReset()).isTrue();
        assertThat(response.resetStage()).isOne();
        assertThat(response.currentStreak()).isZero();
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getNickname()).isNull();
        assertThat(user.getSkinConcerns()).isEmpty();
        assertThat(user.isPersonalizationCompleted()).isFalse();
        verify(pendingMissionCandidateStore).clearForUser(USER_ID);
        verify(todayMissionPreferenceStore).clearForUser(USER_ID);
    }

    @Test
    void doesNotDeleteAnythingWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dataResetService.reset(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        verify(dailyRecordRepository, never()).deleteByUserId(USER_ID);
        verify(forecastRepository, never()).deleteByUser_Id(USER_ID);
        verify(missionCardRepository, never()).deleteByUser_Id(USER_ID);
        verify(pendingMissionCandidateStore, never()).clearForUser(USER_ID);
        verify(todayMissionPreferenceStore, never()).clearForUser(USER_ID);
    }

    private User personalizedUser() {
        return User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("박수현")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS, SkinConcern.TROUBLE))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }
}
