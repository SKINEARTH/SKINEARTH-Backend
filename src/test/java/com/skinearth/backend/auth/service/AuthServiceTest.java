package com.skinearth.backend.auth.service;

import com.skinearth.backend.auth.dto.LoginRequest;
import com.skinearth.backend.auth.dto.LoginResponse;
import com.skinearth.backend.auth.dto.SignupRequest;
import com.skinearth.backend.auth.exception.UnauthorizedException;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void signupNormalizesEmailAndHashesPassword() {
        SignupRequest request = signupRequest("  USER@Example.COM ", "password123!", "password123!");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(passwordEncoder.matches("password123!", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.isResearchDataAgreed()).isFalse();
    }

    @Test
    void rejectsMismatchedPasswordConfirmation() {
        SignupRequest request = signupRequest("user@example.com", "password123!", "different123!");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    @Test
    void rejectsDuplicateEmail() {
        SignupRequest request = signupRequest("user@example.com", "password123!", "password123!");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    void loginReturnsBearerToken() {
        User user = registeredUser();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.issueAccessToken(user)).thenReturn("access-token");

        LoginResponse response = authService.login(new LoginRequest("USER@example.com", "password123!"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void rejectsInvalidPasswordWithoutRevealingWhichCredentialFailed() {
        User user = registeredUser();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong-password")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    private SignupRequest signupRequest(String email, String password, String passwordConfirm) {
        return new SignupRequest(email, password, passwordConfirm, true, true, false);
    }

    private User registeredUser() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("password123!"))
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
