package com.skinearth.backend.forecast.coldstart;

import java.util.List;

public record ColdStartResult(
        int riskScore,
        String riskLevel,
        List<ColdStartFactorResult> primaryFactors
) {
}
