package com.skinearth.backend.user.service;

import com.skinearth.backend.badge.entity.Badge;
import com.skinearth.backend.badge.repository.BadgeRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.service.RecordStreakCalculator;
import com.skinearth.backend.user.dto.MyPageResponse;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 14);

    @Mock
    private UserRepository userRepository;
    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private DailyRecordRepository dailyRecordRepository;

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        myPageService = new MyPageService(
                userRepository,
                badgeRepository,
                dailyRecordRepository,
                new RecordStreakCalculator(),
                clock
        );
    }

    @Test
    void returnsAccountProfileBadgeAndStreakFromStoredData() {
        User user = personalizedUser();
        Badge badge = Badge.builder().stage(1).name("관측자").description("첫 단계").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(badgeRepository.findByStage(1)).thenReturn(Optional.of(badge));
        when(dailyRecordRepository.findRecordDatesUpTo(USER_ID, TODAY)).thenReturn(List.of(
                TODAY,
                TODAY.minusDays(1),
                TODAY.minusDays(2),
                TODAY.minusDays(3)
        ));

        MyPageResponse response = myPageService.get(USER_ID);

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.joinedDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.nickname()).isEqualTo("박수현");
        assertThat(response.skinConcerns()).containsExactlyInAnyOrder(
                SkinConcern.DRYNESS,
                SkinConcern.SENSITIVITY
        );
        assertThat(response.stage()).isOne();
        assertThat(response.badgeName()).isEqualTo("관측자");
        assertThat(response.currentStreak()).isEqualTo(4);
    }

    @Test
    void reportsMissingBadgeConfiguration() {
        User user = personalizedUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(badgeRepository.findByStage(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myPageService.get(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("현재 단계의 뱃지 정보를 찾을 수 없습니다.");
    }

    private User personalizedUser() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("박수현")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS, SkinConcern.SENSITIVITY))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2026, 8, 1, 10, 0));
        return user;
    }
}
