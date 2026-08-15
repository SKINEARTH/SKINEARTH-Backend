package com.skinearth.backend.forecast.entity;

import com.skinearth.backend.forecast.coldstart.ColdStartFactorResult;
import com.skinearth.backend.forecast.coldstart.ForecastFactorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "forecast_factor")
@Getter
@NoArgsConstructor
public class ForecastFactor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "forecast_id", nullable = false)
    private Forecast forecast;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ForecastFactorType factor;
    private Integer priorityScore;
    @Column(nullable = false) private int factorRank;
    @Column(nullable = false) private boolean primaryFactor;

    ForecastFactor(Forecast forecast, ColdStartFactorResult result, int rank) {
        this.forecast = forecast; this.factor = result.factor(); this.priorityScore = result.priorityScore();
        this.factorRank = rank; this.primaryFactor = true;
    }
}
