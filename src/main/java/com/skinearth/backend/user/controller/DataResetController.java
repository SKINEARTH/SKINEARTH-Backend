package com.skinearth.backend.user.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.user.dto.DataResetRequest;
import com.skinearth.backend.user.dto.DataResetResponse;
import com.skinearth.backend.user.service.DataResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/data-reset")
@RequiredArgsConstructor
public class DataResetController {

    private final DataResetService dataResetService;

    @PostMapping
    public ApiResponse<DataResetResponse> reset(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DataResetRequest request
    ) {
        return ApiResponse.success(200, "사용자 데이터를 초기화했습니다.", dataResetService.reset(userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
