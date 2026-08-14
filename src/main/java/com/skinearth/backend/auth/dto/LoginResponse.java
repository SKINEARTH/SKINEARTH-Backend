package com.skinearth.backend.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
