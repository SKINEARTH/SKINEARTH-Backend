package com.skinearth.backend.user.dto;

import jakarta.validation.constraints.AssertTrue;

public record DataResetRequest(
        @AssertTrue(message = "데이터 초기화 확인이 필요합니다.")
        boolean confirmed
) {
}
