package com.skinearth.backend.auth.service;

import com.skinearth.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes";

    @Test
    void issuesTokenContainingUserIdAndEmail() {
        SecretKey secretKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtService jwtService = new JwtService(encoder);
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);

        String token = jwtService.issueAccessToken(user);
        var jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("user@example.com");
        assertThat(jwt.getClaimAsString("scope")).isEqualTo("USER");
    }
}
