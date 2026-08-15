package com.skinearth.backend.mission.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mission_template", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cause", "category", "action_type", "intensity", "timing"})
})
@Getter
@NoArgsConstructor
public class MissionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String cause;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String actionType;

    @Column(nullable = false, length = 20)
    private String intensity;

    @Column(nullable = false, length = 20)
    private String timing;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private int estimatedMinutes = 1;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public MissionTemplate(String cause, String category, String actionType,
                           String intensity, String timing, Integer estimatedMinutes, Boolean isActive) {
        this.cause = cause;
        this.category = category;
        this.actionType = actionType;
        this.intensity = intensity;
        this.timing = timing;
        this.estimatedMinutes = estimatedMinutes == null ? 1 : estimatedMinutes;
        this.isActive = isActive;
    }
}
