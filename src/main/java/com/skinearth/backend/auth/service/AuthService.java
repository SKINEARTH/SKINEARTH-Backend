package com.skinearth.backend.auth.service;

import com.skinearth.backend.auth.dto.LoginRequest;
import com.skinearth.backend.auth.dto.LoginResponse;
import com.skinearth.backend.auth.dto.SignupRequest;
import com.skinearth.backend.auth.dto.SignupResponse;
import com.skinearth.backend.auth.exception.UnauthorizedException;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        if (!request.password().equals(request.passwordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .serviceTermsAgreed(request.serviceTermsAgreed())
                .sensitiveDataAgreed(request.sensitiveDataAgreed())
                .researchDataAgreed(request.researchDataAgreed())
                .build();

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new SignupResponse(savedUser.getId(), savedUser.getEmail());
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return new LoginResponse(
                jwtService.issueAccessToken(user),
                "Bearer",
                JwtService.ACCESS_TOKEN_EXPIRES_IN,
                user.isPersonalizationCompleted()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
