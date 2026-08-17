package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.Forecast;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Builder
public class ForecastResponseDto {
    private Long id;
    private LocalDate targetDate;
    private Integer inputAc;
    private Integer inputScreenTime;
    private Integer inputSleepHours;
    private Integer inputStress;
    private Integer inputMeal;
    private Integer riskScore;
    private String riskLevel;
    private String source;
    private Integer validRecordCount;
    private String aiComment;
    private Boolean isCommentFallback;
    private LocalDateTime createdAt;
    private List<ForecastFactorResponse> primaryFactors;

    public static ForecastResponseDto from(Forecast forecast) {
        List<ForecastFactorResponse> factors = forecast.getFactors().stream()
                .map(ForecastFactorResponse::from).toList();
        if (factors.isEmpty()) {
            List<ForecastFactorResponse> dataFactors = new ArrayList<>();
            if (forecast.getPrimaryFactor1Name() != null)
                dataFactors.add(new ForecastFactorResponse(forecast.getPrimaryFactor1Name(),
                        forecast.getPrimaryFactor1Level(), null, 1));
            if (forecast.getPrimaryFactor2Name() != null)
                dataFactors.add(new ForecastFactorResponse(forecast.getPrimaryFactor2Name(),
                        forecast.getPrimaryFactor2Level(), null, 2));
            factors = List.copyOf(dataFactors);
        }
        return ForecastResponseDto.builder()
                .id(forecast.getId())
                .targetDate(forecast.getTargetDate())
                .inputAc(forecast.getInputAc())
                .inputScreenTime(forecast.getInputScreenTime())
                .inputSleepHours(forecast.getInputSleepHours())
                .inputStress(forecast.getInputStress())
                .inputMeal(forecast.getInputMeal())
                .riskScore(forecast.getRiskScore())
                .riskLevel(forecast.getRiskLevel())
                .source(forecast.getSource())
                .validRecordCount(forecast.getValidRecordCount())
                .aiComment(forecast.getAiComment())
                .isCommentFallback(forecast.getIsCommentFallback())
                .createdAt(forecast.getCreatedAt())
                .primaryFactors(factors)
                .build();
    }
}
