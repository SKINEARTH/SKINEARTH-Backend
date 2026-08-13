package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.Forecast;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
                .build();
    }
}
