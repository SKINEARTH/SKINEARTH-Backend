package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.Forecast;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ForecastResponseDto {
    private Long id;
    private LocalDate targetDate;
    private Integer riskScore;
    private String riskLevel;
    private String source;
    private Integer validRecordCount;
    private String aiComment;
    private Boolean isCommentFallback;
    private LocalDateTime createdAt;
    private List<ForecastFactorResponse> primaryFactors;

    public static ForecastResponseDto from(Forecast forecast) {
        return ForecastResponseDto.builder()
                .id(forecast.getId())
                .targetDate(forecast.getTargetDate())
                .riskScore(forecast.getRiskScore())
                .riskLevel(forecast.getRiskLevel())
                .source(forecast.getSource())
                .validRecordCount(forecast.getValidRecordCount())
                .aiComment(forecast.getAiComment())
                .isCommentFallback(forecast.getIsCommentFallback())
                .createdAt(forecast.getCreatedAt())
                .primaryFactors(forecast.getFactors().stream().map(ForecastFactorResponse::from).toList())
                .build();
    }
}
