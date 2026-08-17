package com.skinearth.backend.forecast.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.dailyrecord.repository.DailyRecordRepository;
import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.forecast.ai.ForecastCommentPromptBuilder;
import com.skinearth.backend.forecast.ai.GeminiClient;
import com.skinearth.backend.forecast.coldstart.ColdStartCalculator;
import com.skinearth.backend.forecast.coldstart.ColdStartResult;
import com.skinearth.backend.forecast.coldstart.ForecastFactorType;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skinearth.backend.forecast.statistics.FactorCorrelation;
import com.skinearth.backend.forecast.statistics.RiskScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastService {
    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);
    private static final int DATA_BASED_RECORD_COUNT = 10;
    private static final double SLEEP_OPTIMAL_HOURS = 7.0;
    private static final String FALLBACK_COMMENT = "아직 데이터를 분석하는 중이에요. 오늘도 꾸준히 기록하며 피부 컨디션을 함께 살펴봐요!";
    private static final Map<ForecastFactorType, String> FACTOR_NAME_KO = Map.of(
            ForecastFactorType.AC, "냉난방 노출",
            ForecastFactorType.SCREEN_TIME, "화면 노출",
            ForecastFactorType.SLEEP, "수면 시간",
            ForecastFactorType.STRESS, "스트레스",
            ForecastFactorType.MEAL_REGULARITY, "식사 규칙성"
    );

    private final ForecastRepository forecastRepository;
    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ColdStartCalculator coldStartCalculator;
    private final Clock clock;
    private final GeminiClient geminiClient;
    private final ForecastCommentPromptBuilder promptBuilder;
    private final RiskScoreCalculator riskScoreCalculator = new RiskScoreCalculator();

    @Transactional
    public ForecastResponseDto createForecast(Long userId, ForecastRequestDto request) {
        LocalDate targetDate = LocalDate.now(clock).plusDays(1);
        User user = findUser(userId);
        if (!user.isPersonalizationCompleted())
            throw new IllegalArgumentException("개인화 설문을 먼저 완료해 주세요.");

        if (forecastRepository.findByUser_IdAndTargetDate(userId, targetDate).isPresent()) {
            throw new IllegalArgumentException("내일의 예보가 이미 존재합니다.");
        }

        Forecast forecast = Forecast.builder()
                .user(user)
                .targetDate(targetDate)
                .build();

        forecast.updateInputs(
                request.inputAc(),
                request.inputScreenTime(),
                request.inputSleepHours(),
                request.inputStress(),
                request.inputMeal()
        );

        recalculateForecast(forecast, user, request, userId);

        return ForecastResponseDto.from(forecastRepository.save(forecast));
    }

    @Transactional
    public ForecastResponseDto updateForecast(Long userId, ForecastRequestDto request) {
        LocalDate targetDate = LocalDate.now(clock).plusDays(1);
        User user = findUser(userId);
        if (!user.isPersonalizationCompleted())
            throw new IllegalArgumentException("개인화 설문을 먼저 완료해 주세요.");

        Forecast forecast = forecastRepository.findByUser_IdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new NotFoundException("내일의 예보가 없습니다. 먼저 예보를 생성해 주세요."));

        forecast.updateInputs(
                request.inputAc(),
                request.inputScreenTime(),
                request.inputSleepHours(),
                request.inputStress(),
                request.inputMeal()
        );

        recalculateForecast(forecast, user, request, userId);

        return ForecastResponseDto.from(forecastRepository.save(forecast));
    }

    private void recalculateForecast(Forecast forecast, User user, ForecastRequestDto request, Long userId) {
        long recordCount = dailyRecordRepository.countByUserId(userId);
        forecast.clearPrimaryFactors();

        if (recordCount < DATA_BASED_RECORD_COUNT) {
            applyColdStartRisk(forecast, user, request, recordCount);
        } else {
            applyDataBasedRisk(forecast, userId, request, recordCount);
        }
        applyAiComment(forecast, user);
    }

    private void applyColdStartRisk(Forecast forecast, User user, ForecastRequestDto request, long recordCount) {
        ColdStartResult result = coldStartCalculator.calculate(user.getUserStatus(), user.getSkinConcerns(), request);
        var first = result.primaryFactors().get(0);
        var second = result.primaryFactors().size() > 1 ? result.primaryFactors().get(1) : null;
        forecast.applyRiskResult(result.riskScore(), result.riskLevel(), "COLD_START", (int) recordCount,
                FACTOR_NAME_KO.get(first.factor()), levelOf(first.normalizedRiskValue()),
                second == null ? null : FACTOR_NAME_KO.get(second.factor()),
                second == null ? null : levelOf(second.normalizedRiskValue()));
        forecast.addPrimaryFactors(result.primaryFactors());
    }

    public ForecastResponseDto getForecast(Long userId) {
        LocalDate targetDate = LocalDate.now(clock).plusDays(1);
        return ForecastResponseDto.from(forecastRepository.findByUser_IdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new NotFoundException("내일의 예보를 찾을 수 없습니다.")));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private void applyDataBasedRisk(Forecast forecast, Long userId, ForecastRequestDto request, long recordCount) {
        List<DailyRecord> records = dailyRecordRepository.findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                userId, LocalDate.now(clock).minusDays(30), LocalDate.now(clock));
        List<FactorCorrelation> all = new ArrayList<>();
        all.add(factor("냉난방 노출", records, DailyRecord::getAcLevel, request.inputAc(), FactorType.NORMAL));
        all.add(factor("화면 노출", records, DailyRecord::getScreenTime, request.inputScreenTime(), FactorType.NORMAL));
        all.add(factor("수면 시간", records, DailyRecord::getSleepHours, request.inputSleepHours(), FactorType.SLEEP));
        all.add(factor("스트레스", records, DailyRecord::getStressLevel, request.inputStress(), FactorType.NORMAL));
        all.add(factor("식사 규칙성", records, DailyRecord::getMealRegularity, request.inputMeal(), FactorType.INVERSE));
        List<FactorCorrelation> primary = riskScoreCalculator.selectPrimaryFactors(all);
        if (primary.isEmpty()) {
            forecast.applyRiskResult(50, "보통", "데이터 기반", (int) recordCount, null, null, null, null);
            return;
        }
        double score = riskScoreCalculator.calculateRiskScore(primary);
        String factor2Name = primary.size() > 1 ? primary.get(1).variableName() : null;
        String factor2Level = primary.size() > 1 ? riskScoreCalculator.determineRiskLevel(primary.get(1).normalizedInput()) : null;
        forecast.applyRiskResult((int) Math.round(score), riskScoreCalculator.determineRiskLevel(score), "데이터 기반",
                (int) recordCount, primary.get(0).variableName(),
                riskScoreCalculator.determineRiskLevel(primary.get(0).normalizedInput()), factor2Name, factor2Level);
    }

    private enum FactorType { NORMAL, INVERSE, SLEEP }

    private FactorCorrelation factor(String name, List<DailyRecord> records,
                                     Function<DailyRecord, Integer> extractor, Integer input, FactorType type) {
        List<Double> x = records.stream().map(extractor).filter(Objects::nonNull).map(Integer::doubleValue).toList();
        List<Double> y = records.stream().map(DailyRecord::getSkinCondition).map(Integer::doubleValue).toList();
        double correlation = x.size() == y.size() && !x.isEmpty()
                ? riskScoreCalculator.calculatePearsonCorrelation(x, y) : 0.0;
        double normalized = normalize(input, type);
        return new FactorCorrelation(name, correlation, normalized);
    }

    private double normalize(Integer input, FactorType type) {
        if (input == null) return 50.0;
        return switch (type) {
            case NORMAL -> (input - 1) / 4.0 * 100;
            case INVERSE -> (5 - input) / 4.0 * 100;
            case SLEEP -> input < SLEEP_OPTIMAL_HOURS ? (SLEEP_OPTIMAL_HOURS - input) / SLEEP_OPTIMAL_HOURS * 100 : 0.0;
        };
    }

    private void applyAiComment(Forecast forecast, User user) {
        try {
            String prompt = promptBuilder.build(user.getNickname(), forecast.getRiskScore(), forecast.getRiskLevel(),
                    forecast.getPrimaryFactor1Name(), forecast.getPrimaryFactor1Level(),
                    forecast.getPrimaryFactor2Name(), forecast.getPrimaryFactor2Level());
            forecast.applyAiComment(geminiClient.generateComment(prompt), false);
        } catch (Exception exception) {
            log.warn("Gemini 코멘트 생성 실패, 폴백 문구로 대체합니다.", exception);
            forecast.applyAiComment(FALLBACK_COMMENT, true);
        }
    }

    private String levelOf(double score) {
        int rounded = (int) Math.round(score);
        if (rounded <= 39) return "낮음";
        if (rounded <= 69) return "보통";
        return "높음";
    }
}
