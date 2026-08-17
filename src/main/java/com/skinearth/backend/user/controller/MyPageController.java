package com.skinearth.backend.user.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.user.dto.MyPageResponse;
import com.skinearth.backend.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "마이페이지 정보를 조회했습니다.", myPageService.get(userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
