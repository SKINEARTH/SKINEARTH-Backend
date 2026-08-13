package com.skinearth.backend.badge.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_badge")
@Getter
@NoArgsConstructor
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //FK로 변경 필요
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false)
    private LocalDateTime earnedAt;

    @Builder
    public UserBadge(Long userId, Badge badge) {
        this.userId = userId;
        this.badge = badge;
        this.earnedAt = LocalDateTime.now();
    }
}