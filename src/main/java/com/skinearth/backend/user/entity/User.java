package com.skinearth.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

    @ElementCollection
    @CollectionTable(name = "user_skin_concern", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skin_concern", nullable = false, length = 20)
    private Set<SkinConcern> skinConcerns = EnumSet.noneOf(SkinConcern.class);

    @Column(nullable = false)
    private boolean personalizationCompleted;

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
    public User(String email, String passwordHash, String nickname, UserStatus userStatus,
                Collection<SkinConcern> skinConcerns,
                boolean serviceTermsAgreed, boolean sensitiveDataAgreed, boolean researchDataAgreed) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.userStatus = userStatus;
        replaceSkinConcerns(skinConcerns);
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.sensitiveDataAgreed = sensitiveDataAgreed;
        this.researchDataAgreed = researchDataAgreed;
        this.personalizationCompleted = nickname != null && userStatus != null && !this.skinConcerns.isEmpty();
        this.stage = 0;
    }

    public Set<SkinConcern> getSkinConcerns() {
        return Collections.unmodifiableSet(skinConcerns);
    }

    public void completePersonalization(String nickname, UserStatus userStatus,
                                        Collection<SkinConcern> skinConcerns) {
        this.nickname = nickname;
        this.userStatus = userStatus;
        replaceSkinConcerns(skinConcerns);
        this.personalizationCompleted = true;
    }

    public void updatePersonalization(String nickname, UserStatus userStatus,
                                      Collection<SkinConcern> skinConcerns) {
        completePersonalization(nickname, userStatus, skinConcerns);
    }

    private void replaceSkinConcerns(Collection<SkinConcern> skinConcerns) {
        this.skinConcerns.clear();
        if (skinConcerns != null) {
            this.skinConcerns.addAll(skinConcerns);
        }
    }
}
