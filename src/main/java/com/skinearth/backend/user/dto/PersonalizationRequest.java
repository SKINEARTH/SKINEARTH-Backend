package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.UserStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;

public record PersonalizationRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @NotNull(message = "현재 상태를 선택해주세요.")
        UserStatus userStatus,

        @NotEmpty(message = "피부 고민을 1개 이상 선택해주세요.")
        List<@NotNull(message = "피부 고민 값은 null일 수 없습니다.") SkinConcern> skinConcerns
) {
    @AssertTrue(message = "피부 고민을 중복으로 선택할 수 없습니다.")
    public boolean isSkinConcernSelectionUnique() {
        return skinConcerns == null || new HashSet<>(skinConcerns).size() == skinConcerns.size();
    }
}
