package com.skinearth.backend.history.controller;

import com.skinearth.backend.common.response.ApiResponse;
import com.skinearth.backend.history.dto.HistoryPeriod;
import com.skinearth.backend.history.dto.CauseTimelineItemResponse;
import com.skinearth.backend.history.dto.HistoryResponse;
import com.skinearth.backend.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ApiResponse<HistoryResponse> get(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam HistoryPeriod period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(200, "궤도 히스토리를 조회했습니다.",
                historyService.get(userId(jwt), period, date));
    }

    @GetMapping("/cause-timeline/weekly")
    public ApiResponse<List<CauseTimelineItemResponse>> getWeeklyCauseTimeline(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(200, "주간 주요 원인 변화를 조회했습니다.",
                historyService.getCauseTimeline(userId(jwt), HistoryPeriod.WEEKLY));
    }

    @GetMapping("/cause-timeline/monthly")
    public ApiResponse<List<CauseTimelineItemResponse>> getMonthlyCauseTimeline(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(200, "월간 주요 원인 변화를 조회했습니다.",
                historyService.getCauseTimeline(userId(jwt), HistoryPeriod.MONTHLY));
    }

    private Long userId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 사용자 인증 정보입니다.");
        }
    }
}
