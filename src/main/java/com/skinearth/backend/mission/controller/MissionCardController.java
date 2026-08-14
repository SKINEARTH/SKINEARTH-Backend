package com.skinearth.backend.mission.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.service.MissionCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionCardController {

    private final MissionCardService missionCardService;

    @GetMapping("/today")
    public ApiResponse<MissionCardResponse> getTodayCard(@RequestParam Long userId) {
        return ApiResponse.success(200,"오늘의 미션 카드를 조회했습니다.", missionCardService.getTodayCard(userId));
    }
}