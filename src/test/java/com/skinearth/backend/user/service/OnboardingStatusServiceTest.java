package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.user.dto.OnboardingStatusResponse;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingStatusServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private DailyRecordRepository dailyRecordRepository;

    private OnboardingStatusService onboardingStatusService;

    @BeforeEach
    void setUp() {
        onboardingStatusService = new OnboardingStatusService(userRepository, dailyRecordRepository);
    }

    @Test
    void combinesUserAndRecordProgressForFirstRecordGuide() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(personalizedUser()));
        when(dailyRecordRepository.countByUserId(USER_ID)).thenReturn(4L);

        OnboardingStatusResponse response = onboardingStatusService.get(USER_ID);

        assertThat(response.nickname()).isEqualTo("박수현");
        assertThat(response.validRecordCount()).isEqualTo(4);
        assertThat(response.remainingRecordCount()).isEqualTo(6);
        assertThat(response.targetRecordCount()).isEqualTo(10);
    }

    @Test
    void reportsMissingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> onboardingStatusService.get(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    private User personalizedUser() {
        return User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("박수현")
                .userStatus(UserStatus.STUDENT)
                .skinConcerns(List.of(SkinConcern.SENSITIVITY))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }
}
