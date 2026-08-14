package com.skinearth.backend.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm,

        @AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
        boolean serviceTermsAgreed,

        @AssertTrue(message = "민감정보 처리에 동의해야 합니다.")
        boolean sensitiveDataAgreed,

        boolean researchDataAgreed
) {
}
