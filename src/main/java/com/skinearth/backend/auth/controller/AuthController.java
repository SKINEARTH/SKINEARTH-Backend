package com.skinearth.backend.auth.controller;

import com.skinearth.backend.auth.dto.LoginRequest;
import com.skinearth.backend.auth.dto.LoginResponse;
import com.skinearth.backend.auth.dto.SignupRequest;
import com.skinearth.backend.auth.dto.SignupResponse;
import com.skinearth.backend.auth.service.AuthService;
import com.skinearth.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(201, "회원가입이 완료되었습니다.", authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(200, "로그인에 성공했습니다.", authService.login(request));
    }
}
