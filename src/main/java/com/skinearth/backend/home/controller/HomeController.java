package com.skinearth.backend.home.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.home.dto.HomeResponse;
import com.skinearth.backend.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "홈 대시보드를 조회했습니다.", homeService.get(userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
