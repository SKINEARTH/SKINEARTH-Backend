package com.skinearth.backend.badge.controller;

import com.skinearth.backend.badge.dto.UserBadgeResponseDto;
import com.skinearth.backend.badge.service.BadgeService;
import com.skinearth.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ApiResponse<List<UserBadgeResponseDto>> getUserBadges(@PathVariable Long userId) {
        List<UserBadgeResponseDto> result = badgeService.getUserBadges(userId);
        return ApiResponse.success(200, "유저의 뱃지를 성공적으로 조회했습니다.", result);
    }
}