package com.skinearth.backend.mission.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mission_template")
@Getter
@NoArgsConstructor
public class MissionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cause;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private String intensity;

    @Column(nullable = false)
    private String timing;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public MissionTemplate(String cause, String category, String actionType,
                           String intensity, String timing, Boolean isActive) {
        this.cause = cause;
        this.category = category;
        this.actionType = actionType;
        this.intensity = intensity;
        this.timing = timing;
        this.isActive = isActive;
    }
}