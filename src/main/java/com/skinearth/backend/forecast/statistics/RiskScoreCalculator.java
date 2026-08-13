package com.skinearth.backend.forecast.statistics;

import java.util.List;

public class RiskScoreCalculator {

    private static final double CORRELATION_THRESHOLD = 0.2;
    private static final int MAX_PRIMARY_FACTORS = 2;

    public double calculatePearsonCorrelation(List<Double> x, List<Double> y) {
        int n = x.size();

        double xMean = x.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double yMean = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerator = 0;
        double xSquaredSum = 0;
        double ySquaredSum = 0;

        for (int i = 0; i < n; i++) {
            double xDiff = x.get(i) - xMean;
            double yDiff = y.get(i) - yMean;

            numerator += xDiff * yDiff;
            xSquaredSum += xDiff * xDiff;
            ySquaredSum += yDiff * yDiff;
        }

        double denominator = Math.sqrt(xSquaredSum * ySquaredSum);

        if (denominator == 0) {
            return 0.0;
        }
        return numerator / denominator;
    }

    public List<FactorCorrelation> selectPrimaryFactors(List<FactorCorrelation> allFactors) {
        return allFactors.stream()
                .filter(f -> Math.abs(f.correlation()) >= CORRELATION_THRESHOLD)
                .sorted((a, b) -> Double.compare(Math.abs(b.correlation()), Math.abs(a.correlation())))
                .limit(MAX_PRIMARY_FACTORS)
                .toList();
    }

    public double calculateRiskScore(List<FactorCorrelation> primaryFactors) {
        if (primaryFactors.isEmpty()) {
            return 50.0;
        }

        double weightedSum = 0;
        double weightSum = 0;

        for (FactorCorrelation factor : primaryFactors) {
            double correctedValue = factor.correlation() >= 0
                    ? 100 - factor.normalizedInput()   // 양수(보호 요인) → 뒤집기
                    : factor.normalizedInput();         // 음수(위험 요인) → 그대로

            double weight = Math.abs(factor.correlation());

            weightedSum += weight * correctedValue;
            weightSum += weight;
        }

        return weightedSum / weightSum;
    }

    public String determineRiskLevel(double riskScore) {
        if (riskScore < 40) {
            return "낮음";
        } else if (riskScore < 70) {
            return "보통";
        } else {
            return "높음";
        }
    }
}