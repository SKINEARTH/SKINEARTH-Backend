package com.skinearth.backend.badge.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "badge")
@NoArgsConstructor
@Getter
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,  unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;
    private Integer threshold;

    @Builder
    public Badge(String code, String name, String description, Integer threshold) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.threshold = threshold;
    }
}
