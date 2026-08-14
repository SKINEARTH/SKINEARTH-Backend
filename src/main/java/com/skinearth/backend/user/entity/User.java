package com.skinearth.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus userStatus;

    @Column(length = 50)
    private String skinConcern;

    @AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
    @Column(nullable = false)
    private boolean serviceTermsAgreed;

    @AssertTrue(message = "민감정보 처리에 동의해야 합니다.")
    @Column(nullable = false)
    private boolean sensitiveDataAgreed;

    @Column(nullable = false)
    private boolean researchDataAgreed;

    @Min(0)
    @Max(3)
    @Column(nullable = false)
    private int stage;

    @Builder
    public User(String email, String passwordHash, String nickname, UserStatus userStatus, String skinConcern,
                boolean serviceTermsAgreed, boolean sensitiveDataAgreed, boolean researchDataAgreed) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.userStatus = userStatus;
        this.skinConcern = skinConcern;
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.sensitiveDataAgreed = sensitiveDataAgreed;
        this.researchDataAgreed = researchDataAgreed;
        this.stage = 0;
    }
}
