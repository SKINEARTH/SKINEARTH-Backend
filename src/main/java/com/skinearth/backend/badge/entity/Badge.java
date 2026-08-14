package com.skinearth.backend.badge.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "badge")
@Getter
@NoArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer stage;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 255)
    private String conditionDescription;

    private Integer recordCountThreshold;
    private Integer streakThreshold;
    private Integer missionCountThreshold;

    @Builder
    public Badge(Integer stage, String name, String description, String conditionDescription,
                 Integer recordCountThreshold, Integer streakThreshold, Integer missionCountThreshold) {
        this.stage = stage;
        this.name = name;
        this.description = description;
        this.conditionDescription = conditionDescription;
        this.recordCountThreshold = recordCountThreshold;
        this.streakThreshold = streakThreshold;
        this.missionCountThreshold = missionCountThreshold;
    }
}