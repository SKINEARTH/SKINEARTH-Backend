package com.skinearth.backend.user.repository;

import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.entity.SkinConcern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUser() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .nickname("스킨어스")
                .userStatus(UserStatus.EMPLOYEE)
                .skinConcerns(List.of(SkinConcern.SENSITIVITY, SkinConcern.DRYNESS))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findById(savedUser.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getNickname()).isEqualTo("스킨어스");
                    assertThat(found.getEmail()).isEqualTo("user@example.com");
                    assertThat(found.getUserStatus()).isEqualTo(UserStatus.EMPLOYEE);
                    assertThat(found.getSkinConcerns()).containsExactlyInAnyOrder(
                            SkinConcern.SENSITIVITY,
                            SkinConcern.DRYNESS
                    );
                    assertThat(found.isPersonalizationCompleted()).isTrue();
                    assertThat(found.isResearchDataAgreed()).isFalse();
                    assertThat(found.getStage()).isZero();
                });
    }
}
