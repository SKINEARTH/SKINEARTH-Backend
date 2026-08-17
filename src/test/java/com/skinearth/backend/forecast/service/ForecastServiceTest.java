package com.skinearth.backend.forecast.service;

import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.ai.ForecastCommentPromptBuilder;
import com.skinearth.backend.forecast.ai.GeminiClient;
import com.skinearth.backend.forecast.coldstart.ColdStartCalculator;
import com.skinearth.backend.forecast.coldstart.ColdStartFactorResult;
import com.skinearth.backend.forecast.coldstart.ColdStartResult;
import com.skinearth.backend.forecast.coldstart.ForecastFactorType;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 8, 17);

    @Mock
    private ForecastRepository forecastRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DailyRecordRepository dailyRecordRepository;
    @Mock
    private ColdStartCalculator coldStartCalculator;
    @Mock
    private GeminiClient geminiClient;

    private ForecastService forecastService;
    private User user;
    private ForecastRequestDto request;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-16T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        forecastService = new ForecastService(
                forecastRepository,
                userRepository,
                dailyRecordRepository,
                coldStartCalculator,
                clock,
                geminiClient,
                new ForecastCommentPromptBuilder()
        );
        user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("테스트")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.DRYNESS))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
        request = new ForecastRequestDto(5, 4, 6, 3, 2);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dailyRecordRepository.countByUserId(USER_ID)).thenReturn(3L);
        when(coldStartCalculator.calculate(any(), any(), any())).thenReturn(coldStartResult());
        when(geminiClient.generateComment(any())).thenReturn("새로 계산된 예보 코멘트입니다.");
    }

    @Test
    void createsForecastWhenTomorrowForecastDoesNotExist() {
        when(forecastRepository.findByUser_IdAndTargetDate(USER_ID, TARGET_DATE)).thenReturn(Optional.empty());
        when(forecastRepository.save(any(Forecast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = forecastService.createForecast(USER_ID, request);

        assertThat(response.getTargetDate()).isEqualTo(TARGET_DATE);
        assertThat(response.getInputAc()).isEqualTo(5);
        assertThat(response.getInputScreenTime()).isEqualTo(4);
        assertThat(response.getInputSleepHours()).isEqualTo(6);
        assertThat(response.getInputStress()).isEqualTo(3);
        assertThat(response.getInputMeal()).isEqualTo(2);
        assertThat(response.getPrimaryFactors()).hasSize(1);
    }

    @Test
    void recalculatesExistingForecastAndReplacesPreviousColdStartFactors() {
        Forecast existing = Forecast.builder()
                .user(user)
                .targetDate(TARGET_DATE)
                .inputAc(1)
                .inputScreenTime(1)
                .inputSleepHours(8)
                .inputStress(1)
                .inputMeal(5)
                .build();
        existing.addPrimaryFactors(List.of(
                new ColdStartFactorResult(ForecastFactorType.SLEEP, 5, 0, 70.0)
        ));
        when(forecastRepository.findByUser_IdAndTargetDate(USER_ID, TARGET_DATE)).thenReturn(Optional.of(existing));
        when(forecastRepository.save(existing)).thenReturn(existing);

        var response = forecastService.updateForecast(USER_ID, request);

        assertThat(response.getInputAc()).isEqualTo(5);
        assertThat(response.getInputScreenTime()).isEqualTo(4);
        assertThat(response.getInputSleepHours()).isEqualTo(6);
        assertThat(response.getInputStress()).isEqualTo(3);
        assertThat(response.getInputMeal()).isEqualTo(2);
        assertThat(existing.getFactors()).hasSize(1);
        assertThat(existing.getFactors().get(0).getFactor()).isEqualTo(ForecastFactorType.AC);
        assertThat(existing.getAiComment()).isEqualTo("새로 계산된 예보 코멘트입니다.");
        verify(forecastRepository).save(existing);
    }

    @Test
    void rejectsUpdateWhenTomorrowForecastDoesNotExist() {
        reset(dailyRecordRepository, coldStartCalculator, geminiClient);
        when(forecastRepository.findByUser_IdAndTargetDate(USER_ID, TARGET_DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.updateForecast(USER_ID, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsCreateWhenTomorrowForecastAlreadyExists() {
        reset(dailyRecordRepository, coldStartCalculator, geminiClient);
        Forecast existing = Forecast.builder().user(user).targetDate(TARGET_DATE).build();
        when(forecastRepository.findByUser_IdAndTargetDate(USER_ID, TARGET_DATE)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> forecastService.createForecast(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내일의 예보가 이미 존재합니다.");
    }

    private ColdStartResult coldStartResult() {
        return new ColdStartResult(
                75,
                "높음",
                List.of(new ColdStartFactorResult(ForecastFactorType.AC, 7, 1, 100.0))
        );
    }
}
