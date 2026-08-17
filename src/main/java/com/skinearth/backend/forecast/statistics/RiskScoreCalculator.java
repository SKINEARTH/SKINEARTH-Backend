package com.skinearth.backend.forecast.statistics;

import java.util.List;

public class RiskScoreCalculator {

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
                .sorted((a, b) -> {
                    int riskComparison = Double.compare(b.normalizedInput(), a.normalizedInput());
                    if (riskComparison != 0) {
                        return riskComparison;
                    }
                    return Double.compare(Math.abs(b.correlation()), Math.abs(a.correlation()));
                })
                .limit(MAX_PRIMARY_FACTORS)
                .toList();
    }

    public double calculateRiskScore(List<FactorCorrelation> factors) {
        if (factors.isEmpty()) {
            return 50.0;
        }

        double weightedSum = 0;
        double weightSum = 0;

        for (FactorCorrelation factor : factors) {
            double weight = Math.abs(factor.correlation());

            weightedSum += weight * factor.normalizedInput();
            weightSum += weight;
        }

        if (weightSum == 0) {
            return factors.stream()
                    .mapToDouble(FactorCorrelation::normalizedInput)
                    .average()
                    .orElse(50.0);
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
