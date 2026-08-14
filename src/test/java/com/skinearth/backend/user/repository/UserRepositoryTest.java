package com.skinearth.backend.user.repository;

import com.skinearth.backend.user.entity.SkinType;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
                .nickname("스킨어스")
                .userStatus(UserStatus.EMPLOYEE)
                .skinType(SkinType.NORMAL)
                .skinConcern("민감함")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .thirdPartyDataAgreed(false)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findById(savedUser.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getNickname()).isEqualTo("스킨어스");
                    assertThat(found.getUserStatus()).isEqualTo(UserStatus.EMPLOYEE);
                    assertThat(found.getSkinType()).isEqualTo(SkinType.NORMAL);
                    assertThat(found.getStage()).isZero();
                });
    }
}
