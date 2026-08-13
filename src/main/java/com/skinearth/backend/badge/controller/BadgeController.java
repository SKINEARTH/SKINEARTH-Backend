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
}