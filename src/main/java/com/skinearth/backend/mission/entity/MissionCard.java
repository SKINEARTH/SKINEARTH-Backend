package com.skinearth.backend.mission.entity;

import com.skinearth.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mission_card",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mission_card_user_date",
                columnNames = {"user_id", "issued_date"}
        )
)
@Getter
@NoArgsConstructor
public class MissionCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private MissionTemplate template;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isCompleted;

    private LocalDateTime completedAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isReplaced;

    @Builder
    public MissionCard(User user, MissionTemplate template, LocalDate issuedDate,
                       String title, String description, Boolean isCompleted, Boolean isReplaced) {
        this.user = user;
        this.template = template;
        this.issuedDate = issuedDate;
        this.title = title;
        this.description = description;
        this.isCompleted = Boolean.TRUE.equals(isCompleted);
        this.isReplaced = Boolean.TRUE.equals(isReplaced);
    }

    public void complete(LocalDateTime completedAt) {
        if (Boolean.TRUE.equals(isCompleted)) {
            throw new IllegalStateException("이미 완료한 미션입니다.");
        }
        this.isCompleted = true;
        this.completedAt = completedAt;
    }

    public void updateContent(MissionTemplate template, String title, String description) {
        this.template = template;
        this.title = title;
        this.description = description;
    }
}
