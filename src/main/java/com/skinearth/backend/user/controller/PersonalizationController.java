package com.skinearth.backend.user.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.user.dto.PersonalizationRequest;
import com.skinearth.backend.user.dto.PersonalizationResponse;
import com.skinearth.backend.user.service.PersonalizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/personalization")
@RequiredArgsConstructor
public class PersonalizationController {

    private final PersonalizationService personalizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PersonalizationResponse> complete(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PersonalizationRequest request
    ) {
        return ApiResponse.success(201, "개인화 설문을 완료했습니다.",
                personalizationService.complete(userId(jwt), request));
    }

    @GetMapping
    public ApiResponse<PersonalizationResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "개인화 정보를 조회했습니다.",
                personalizationService.get(userId(jwt)));
    }

    @PutMapping
    public ApiResponse<PersonalizationResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PersonalizationRequest request
    ) {
        return ApiResponse.success(200, "개인화 정보를 수정했습니다.",
                personalizationService.update(userId(jwt), request));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
