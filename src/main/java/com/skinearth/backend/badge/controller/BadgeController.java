package com.skinearth.backend.badge.controller;

import com.skinearth.backend.badge.dto.BadgeResponseDto;
import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/stage")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ApiResponse<BadgeResponseDto> getMyStage(
            @PathVariable Long userId,
            @RequestParam Integer stage) {  // 임시: userId로 진짜 조회 대신 stage를 직접 받음

        BadgeResponseDto result = badgeService.getStageInfo(stage);
        return ApiResponse.success(200, "단계 조회 성공", result);
    }
}