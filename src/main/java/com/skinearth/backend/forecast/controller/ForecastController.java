package com.skinearth.backend.forecast.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastController {
    private final ForecastService forecastService;

    @PostMapping
    public ApiResponse<ForecastResponseDto> createForecast(@RequestBody ForecastRequestDto dto){
        ForecastResponseDto result = forecastService.createForecast(dto);
        return ApiResponse.success(200,"예보가 저장되었습니다.", result);
    }

    @GetMapping
    public ApiResponse<ForecastResponseDto> getForecast(@RequestParam Long userId){
        ForecastResponseDto result = forecastService.getForecast(userId);
        return ApiResponse.success(200, "예보를 성공적으로 조회했습니다.", result);
    }
}
