package com.skinearth.backend.forecast.service;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.forecast.ai.ForecastCommentPromptBuilder;
import com.skinearth.backend.forecast.ai.GeminiClient;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.forecast.statistics.FactorCorrelation;
import com.skinearth.backend.forecast.statistics.RiskScoreCalculator;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ForecastService {
    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);
    private static final int MIN_VALID_RECORD_COUNT = 10;
    private static final double SLEEP_HOURS_MAX = 24.0;
    private static final String FALLBACK_COMMENT = "아직 데이터를 분석하는 중이에요. 오늘도 꾸준히 기록하며 피부 컨디션을 함께 살펴봐요!";

    private final ForecastRepository forecastRepository;
    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final GeminiClient geminiClient;
    private final ForecastCommentPromptBuilder promptBuilder;
    private final RiskScoreCalculator riskScoreCalculator = new RiskScoreCalculator();

    public ForecastResponseDto createForecast(Long userId, ForecastRequestDto dto) {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        if (forecastRepository.existsByUser_IdAndTargetDate(userId, targetDate)) {
            throw new IllegalArgumentException("이미 존재하는 예보입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Forecast forecast = Forecast.builder()
                .user(user)
                .targetDate(targetDate)
                .inputAc(dto.getInputAc())
                .inputScreenTime(dto.getInputScreenTime())
                .inputSleepHours(dto.getInputSleepHours())
                .inputStress(dto.getInputStress())
                .inputMeal(dto.getInputMeal())
                .build();

        applyRiskCalculation(forecast, userId, dto);
        applyAiComment(forecast, user);

        Forecast savedForecast = forecastRepository.save(forecast);
        return ForecastResponseDto.from(savedForecast);
    }

    private void applyRiskCalculation(Forecast forecast, Long userId, ForecastRequestDto dto) {
        long validRecordCount = dailyRecordRepository.countByUserId(userId);

        if (validRecordCount < MIN_VALID_RECORD_COUNT) {
            forecast.applyRiskResult(50, "보통", "추정치", (int) validRecordCount,
                    null, null, null, null);
            return;
        }

        List<DailyRecord> records = dailyRecordRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                        userId, LocalDate.now().minusDays(30), LocalDate.now());

        List<FactorCorrelation> allFactors = new ArrayList<>();
        allFactors.add(buildFactorCorrelation("냉난방", records, DailyRecord::getAcLevel, dto.getInputAc(), false));
        allFactors.add(buildFactorCorrelation("화면 노출", records, DailyRecord::getScreenTime, dto.getInputScreenTime(), false));
        allFactors.add(buildFactorCorrelation("수면", records, DailyRecord::getSleepHours, dto.getInputSleepHours(), true));
        allFactors.add(buildFactorCorrelation("스트레스", records, DailyRecord::getStressLevel, dto.getInputStress(), false));
        allFactors.add(buildFactorCorrelation("식사 규칙성", records, DailyRecord::getMealRegularity, dto.getInputMeal(), false));

        List<FactorCorrelation> primaryFactors = riskScoreCalculator.selectPrimaryFactors(allFactors);

        if (primaryFactors.isEmpty()) {
            forecast.applyRiskResult(50, "보통", "데이터 기반", (int) validRecordCount,
                    null, null, null, null);
            return;
        }

        double riskScore = riskScoreCalculator.calculateRiskScore(primaryFactors);
        String riskLevel = riskScoreCalculator.determineRiskLevel(riskScore);

        String factor1Name = primaryFactors.get(0).variableName();
        String factor1Level = riskScoreCalculator.determineRiskLevel(primaryFactors.get(0).normalizedInput());
        String factor2Name = primaryFactors.size() > 1 ? primaryFactors.get(1).variableName() : null;
        String factor2Level = primaryFactors.size() > 1
                ? riskScoreCalculator.determineRiskLevel(primaryFactors.get(1).normalizedInput())
                : null;

        forecast.applyRiskResult((int) Math.round(riskScore), riskLevel, "데이터 기반", (int) validRecordCount,
                factor1Name, factor1Level, factor2Name, factor2Level);
    }

    private void applyAiComment(Forecast forecast, User user) {
        try {
            String prompt = promptBuilder.build(
                    user.getNickname(),
                    forecast.getRiskScore(),
                    forecast.getRiskLevel(),
                    forecast.getPrimaryFactor1Name(),
                    forecast.getPrimaryFactor1Level(),
                    forecast.getPrimaryFactor2Name(),
                    forecast.getPrimaryFactor2Level()
            );
            String comment = geminiClient.generateComment(prompt);
            forecast.applyAiComment(comment, false);
        } catch (Exception exception) {
            log.warn("Gemini 코멘트 생성 실패, 폴백 문구로 대체합니다.", exception);
            forecast.applyAiComment(FALLBACK_COMMENT, true);
        }
    }

    private FactorCorrelation buildFactorCorrelation(String name, List<DailyRecord> records,
                                                     Function<DailyRecord, Integer> extractor,
                                                     Integer tomorrowInput, boolean isSlider) {
        List<Double> xValues = records.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(Integer::doubleValue)
                .toList();
        List<Double> yValues = records.stream()
                .map(DailyRecord::getSkinCondition)
                .map(Integer::doubleValue)
                .toList();

        double correlation = xValues.size() == yValues.size() && !xValues.isEmpty()
                ? riskScoreCalculator.calculatePearsonCorrelation(xValues, yValues)
                : 0.0;

        double normalizedInput;
        if (tomorrowInput == null) {
            normalizedInput = 50.0;
        } else if (isSlider) {
            normalizedInput = (tomorrowInput / SLEEP_HOURS_MAX) * 100;
        } else {
            normalizedInput = (tomorrowInput - 1) / 4.0 * 100;
        }

        return new FactorCorrelation(name, correlation, normalizedInput);
    }

    public ForecastResponseDto getForecast(Long userId) {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        Forecast forecast = forecastRepository.findByUser_IdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new IllegalArgumentException("해당 예보를 찾을 수 없습니다."));
        return ForecastResponseDto.from(forecast);
    }
}