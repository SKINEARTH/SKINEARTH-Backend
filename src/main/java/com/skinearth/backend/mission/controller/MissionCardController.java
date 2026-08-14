package com.skinearth.backend.mission.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.dto.MissionHistoryResponse;
import com.skinearth.backend.mission.dto.WeeklyMissionHistoryResponse;
import com.skinearth.backend.mission.service.MissionCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionCardController {

    private final MissionCardService missionCardService;

    @GetMapping("/today")
    public ApiResponse<MissionCardResponse> getTodayCard(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(200, "오늘의 미션 카드를 조회했습니다.",
                missionCardService.getTodayCard(userId(jwt)));
    }

    @PostMapping("/{missionCardId}/complete")
    public ApiResponse<MissionHistoryResponse> complete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long missionCardId
    ) {
        return ApiResponse.success(200, "미션 수행을 완료했습니다.",
                missionCardService.complete(userId(jwt), missionCardId));
    }

    @GetMapping("/history/weekly")
    public ApiResponse<WeeklyMissionHistoryResponse> getWeeklyHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(200, "주간 미션 이행 기록을 조회했습니다.",
                missionCardService.getWeeklyHistory(userId(jwt), date));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
