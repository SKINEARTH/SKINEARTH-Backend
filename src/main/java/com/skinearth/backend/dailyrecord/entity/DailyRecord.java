package com.skinearth.backend.dailyrecord.entity;

import com.skinearth.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(
        name = "daily_record",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_record_user_date",
                columnNames = {"user_id", "record_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate recordDate;

    private Integer acLevel;
    private Integer screenTime;
    private Integer sleepHours;
    private Integer stressLevel;
    private Integer mealRegularity;

    @Column(nullable = false)
    private Integer skinCondition;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "daily_record_symptom", joinColumns = @JoinColumn(name = "daily_record_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "symptom", nullable = false, length = 20)
    private Set<SymptomTag> symptoms = EnumSet.noneOf(SymptomTag.class);

    @Builder
    public DailyRecord(User user, LocalDate recordDate, Integer acLevel, Integer screenTime,
                       Integer sleepHours, Integer stressLevel, Integer mealRegularity,
                       Integer skinCondition, Collection<SymptomTag> symptoms) {
        this.user = user;
        this.recordDate = recordDate;
        update(acLevel, screenTime, sleepHours, stressLevel, mealRegularity, skinCondition, symptoms);
    }

    public void update(Integer acLevel, Integer screenTime, Integer sleepHours,
                       Integer stressLevel, Integer mealRegularity, Integer skinCondition,
                       Collection<SymptomTag> symptoms) {
        this.acLevel = acLevel;
        this.screenTime = screenTime;
        this.sleepHours = sleepHours;
        this.stressLevel = stressLevel;
        this.mealRegularity = mealRegularity;
        this.skinCondition = skinCondition;
        this.symptoms.clear();
        if (symptoms != null) {
            this.symptoms.addAll(symptoms);
        }
    }

    public Set<SymptomTag> getSymptoms() {
        return Collections.unmodifiableSet(symptoms);
    }
}
