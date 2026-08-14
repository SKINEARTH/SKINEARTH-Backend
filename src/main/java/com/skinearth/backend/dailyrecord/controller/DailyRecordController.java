package com.skinearth.backend.dailyrecord.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.dailyrecord.dto.DailyRecordRequest;
import com.skinearth.backend.dailyrecord.dto.DailyRecordResponse;
import com.skinearth.backend.dailyrecord.service.DailyRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-records")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    @PostMapping("/today")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DailyRecordResponse> createToday(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DailyRecordRequest request
    ) {
        return ApiResponse.success(201, "오늘의 기록을 저장했습니다.",
                dailyRecordService.createToday(userId(jwt), request));
    }

    @GetMapping("/today")
    public ApiResponse<DailyRecordResponse> getToday(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "오늘의 기록을 조회했습니다.",
                dailyRecordService.getToday(userId(jwt)));
    }

    @PutMapping("/today")
    public ApiResponse<DailyRecordResponse> updateToday(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DailyRecordRequest request
    ) {
        return ApiResponse.success(200, "오늘의 기록을 수정했습니다.",
                dailyRecordService.updateToday(userId(jwt), request));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
