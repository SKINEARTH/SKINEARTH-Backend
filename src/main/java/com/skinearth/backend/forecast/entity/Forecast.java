package com.skinearth.backend.forecast.entity;

import com.skinearth.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Forecast(User user, LocalDate targetDate,
                    Integer inputAc, Integer inputScreenTime, Integer inputSleepHours,
                    Integer inputStress, Integer inputMeal) {
        this.user = user;
        this.targetDate = targetDate;
        this.inputAc = inputAc;
        this.inputScreenTime = inputScreenTime;
        this.inputSleepHours = inputSleepHours;
        this.inputStress = inputStress;
        this.inputMeal = inputMeal;
        this.createdAt = LocalDateTime.now();
    }
}