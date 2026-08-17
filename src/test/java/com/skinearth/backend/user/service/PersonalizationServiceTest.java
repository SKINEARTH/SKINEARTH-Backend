package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.user.dto.PersonalizationRequest;
import com.skinearth.backend.user.dto.PersonalizationResponse;
import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalizationServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    private PersonalizationService personalizationService;

    @BeforeEach
    void setUp() {
        personalizationService = new PersonalizationService(userRepository);
    }

    @Test
    void completesPersonalizationOnce() {
        User user = registeredUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        PersonalizationResponse response = personalizationService.complete(USER_ID, request());

        assertThat(response.nickname()).isEqualTo("여행자");
        assertThat(response.userStatus()).isEqualTo(UserStatus.EMPLOYEE);
        assertThat(response.skinConcerns()).containsExactlyInAnyOrder(
                SkinConcern.DRYNESS,
                SkinConcern.TROUBLE
        );
        assertThat(response.personalizationCompleted()).isTrue();
    }

    @Test
    void rejectsRepeatedInitialCompletion() {
        User user = registeredUser();
        user.completePersonalization("기존", UserStatus.STUDENT, List.of(SkinConcern.PORES));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> personalizationService.complete(USER_ID, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("개인화 설문이 이미 완료되었습니다.");
    }

    @Test
    void updatesCompletedPersonalizationWithoutChangingCompletionState() {
        User user = registeredUser();
        user.completePersonalization("기존", UserStatus.STUDENT, List.of(SkinConcern.PORES));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        PersonalizationResponse response = personalizationService.update(USER_ID, request());

        assertThat(response.nickname()).isEqualTo("여행자");
        assertThat(response.skinConcerns()).doesNotContain(SkinConcern.PORES);
        assertThat(response.personalizationCompleted()).isTrue();
    }

    @Test
    void doesNotExposeIncompletePersonalizationAsCompletedProfile() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(registeredUser()));

        assertThatThrownBy(() -> personalizationService.get(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("완료된 개인화 설문을 찾을 수 없습니다.");
    }

    private PersonalizationRequest request() {
        return new PersonalizationRequest(
                "  여행자  ",
                UserStatus.EMPLOYEE,
                List.of(SkinConcern.DRYNESS, SkinConcern.TROUBLE)
        );
    }

    private User registeredUser() {
        return User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }
}
