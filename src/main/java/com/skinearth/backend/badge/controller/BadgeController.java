package com.skinearth.backend.badge.controller;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/stage")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ApiResponse<BadgeResponseDto> getMyStage(@AuthenticationPrincipal Jwt jwt) {
        BadgeResponseDto result = badgeService.getStageInfo(userId(jwt));
        return ApiResponse.success(200, "단계 조회 성공", result);
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}