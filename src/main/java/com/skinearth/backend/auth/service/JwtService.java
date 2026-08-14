package com.skinearth.backend.auth.service;

import com.skinearth.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final long ACCESS_TOKEN_EXPIRES_IN = 3600L;

    private final JwtEncoder jwtEncoder;

    public String issueAccessToken(User user) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("skinearth")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(ACCESS_TOKEN_EXPIRES_IN))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("scope", "USER")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
