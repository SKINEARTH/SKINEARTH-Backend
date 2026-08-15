package com.skinearth.backend.forecast.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.service.ForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastController {
    private final ForecastService forecastService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ForecastResponseDto> createForecast(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ForecastRequestDto request) {
        return ApiResponse.success(201, "콜드스타트 예보를 저장했습니다.",
                forecastService.createForecast(userId(jwt), request));
    }

    @GetMapping
    public ApiResponse<ForecastResponseDto> getForecast(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "내일의 예보를 조회했습니다.", forecastService.getForecast(userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        try { return Long.valueOf(jwt.getSubject()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다."); }
    }
}
