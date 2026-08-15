package com.skinearth.backend.forecast.dto;

import com.skinearth.backend.forecast.entity.Forecast;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<PrimaryFactor> primaryFactors;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class PrimaryFactor {
        private String name;
        private String level;
    }

    public static ForecastResponseDto from(Forecast forecast) {
        List<PrimaryFactor> factors = new ArrayList<>();
        if (forecast.getPrimaryFactor1Name() != null) {
            factors.add(PrimaryFactor.builder()
                    .name(forecast.getPrimaryFactor1Name())
                    .level(forecast.getPrimaryFactor1Level())
                    .build());
        }
        if (forecast.getPrimaryFactor2Name() != null) {
            factors.add(PrimaryFactor.builder()
                    .name(forecast.getPrimaryFactor2Name())
                    .level(forecast.getPrimaryFactor2Level())
                    .build());
        }

        return ForecastResponseDto.builder()
                .id(forecast.getId())
                .targetDate(forecast.getTargetDate())
                .riskScore(forecast.getRiskScore())
                .riskLevel(forecast.getRiskLevel())
                .source(forecast.getSource())
                .validRecordCount(forecast.getValidRecordCount())
                .aiComment(forecast.getAiComment())
                .isCommentFallback(forecast.getIsCommentFallback())
                .primaryFactors(factors)
                .createdAt(forecast.getCreatedAt())
                .build();
    }
}