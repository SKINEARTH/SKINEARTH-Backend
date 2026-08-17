package com.skinearth.backend.home.service;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.home.dto.PlanetTemperatureResponse;
import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.service.MissionCardService;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {
    @Mock UserRepository userRepository;
    @Mock DailyRecordRepository dailyRecordRepository;
    @Mock ForecastRepository forecastRepository;
    @Mock MissionCardService missionCardService;
    @Mock BadgeService badgeService;
    @Mock PlanetTemperatureCalculator temperatureCalculator;

    private HomeService homeService;
    private final LocalDate today = LocalDate.of(2026, 8, 16);

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-16T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        homeService = new HomeService(userRepository, dailyRecordRepository, forecastRepository,
                missionCardService, badgeService, temperatureCalculator, clock);
    }

    @Test
    void combinesHomeDataAndCapsForecastProgressAtOneHundredPercent() {
        User user = User.builder().email("user@example.com").passwordHash("encoded")
                .nickname("여행자").userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS))
                .serviceTermsAgreed(true).sensitiveDataAgreed(true).build();
        MissionCardResponse mission = new MissionCardResponse(1L, "수분 보충", "미스트 리터치",
                "미스트를 뿌려주세요.", 3, today, false, false, null, 2);
        BadgeResponseDto badge = BadgeResponseDto.builder().stage(2).name("탐사자").progressList(List.of()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dailyRecordRepository.findByUserIdAndRecordDate(1L, today)).thenReturn(Optional.empty());
        when(dailyRecordRepository.countByUserId(1L)).thenReturn(12L);
        when(forecastRepository.findAllByUser_IdAndTargetDateBetweenOrderByTargetDateAsc(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(forecastRepository.findByUser_IdAndTargetDate(1L, today.plusDays(1))).thenReturn(Optional.empty());
        when(temperatureCalculator.calculate(anyList(), eq(today)))
                .thenReturn(new PlanetTemperatureResponse(62, "주의", 5));
        when(missionCardService.getTodayCard(1L)).thenReturn(mission);
        when(badgeService.getStageInfo(1L)).thenReturn(badge);

        var response = homeService.get(1L);

        assertThat(response.nickname()).isEqualTo("여행자");
        assertThat(response.todayRecord().recorded()).isFalse();
        assertThat(response.todayRecord().recordCtaRequired()).isTrue();
        assertThat(response.forecastProgress().progressPercent()).isEqualTo(100);
        assertThat(response.forecastProgress().remainingRecordCount()).isZero();
        assertThat(response.forecastProgress().forecastMode()).isEqualTo("DATA_BASED");
        assertThat(response.todayMission()).isEqualTo(mission);
        assertThat(response.badge()).isEqualTo(badge);
    }
}
