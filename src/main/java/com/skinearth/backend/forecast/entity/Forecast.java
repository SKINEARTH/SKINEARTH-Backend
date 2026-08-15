package com.skinearth.backend.forecast.entity;

import com.skinearth.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.skinearth.backend.forecast.coldstart.ColdStartFactorResult;

@Entity
@Table(name="forecast")
@Getter
@NoArgsConstructor
public class Forecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
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

    @OneToMany(mappedBy = "forecast", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("factorRank ASC")
    private List<ForecastFactor> factors = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Forecast(User user, LocalDate targetDate,
                    Integer inputAc, Integer inputScreenTime, Integer inputSleepHours,
                    Integer inputStress, Integer inputMeal, Integer riskScore,
                    String riskLevel, String source, Integer validRecordCount) {
        this.user = user;
        this.targetDate = targetDate;
        this.inputAc = inputAc;
        this.inputScreenTime = inputScreenTime;
        this.inputSleepHours = inputSleepHours;
        this.inputStress = inputStress;
        this.inputMeal = inputMeal;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.source = source;
        this.validRecordCount = validRecordCount;
        this.createdAt = LocalDateTime.now();
    }

    public void addPrimaryFactors(List<ColdStartFactorResult> results) {
        for (int i = 0; i < results.size(); i++) factors.add(new ForecastFactor(this, results.get(i), i + 1));
    }

    public List<ForecastFactor> getFactors() { return Collections.unmodifiableList(factors); }
}
