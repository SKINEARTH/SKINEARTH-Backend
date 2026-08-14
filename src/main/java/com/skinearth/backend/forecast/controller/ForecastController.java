package com.skinearth.backend.forecast.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastController {
    private final ForecastService forecastService;

    @PostMapping
    public ApiResponse<ForecastResponseDto> createForecast(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ForecastRequestDto dto) {
        ForecastResponseDto result = forecastService.createForecast(userId(jwt), dto);
        return ApiResponse.success(200, "예보가 저장되었습니다.", result);
    }

    @GetMapping
    public ApiResponse<ForecastResponseDto> getForecast(@AuthenticationPrincipal Jwt jwt) {
        ForecastResponseDto result = forecastService.getForecast(userId(jwt));
        return ApiResponse.success(200, "예보를 성공적으로 조회했습니다.", result);
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}