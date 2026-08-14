package com.skinearth.backend.mission.entity;

import com.skinearth.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Builder
    public MissionCard(User user, MissionTemplate template, LocalDate issuedDate,
                       String title, String description, Boolean isCompleted) {
        this.user = user;
        this.template = template;
        this.issuedDate = issuedDate;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
    }
}