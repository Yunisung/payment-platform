package com.payment.api.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "payment-platform-secret-key-must-be-at-least-256-bits-long",
                86400000L
        );
    }

    @Test
    @DisplayName("토큰 생성 후 memberId 추출 성공")
    void createToken_and_getMemberId() {
        String token = jwtTokenProvider.createToken(1L, "MERCHANT");

        assertThat(jwtTokenProvider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("토큰 생성 후 role 추출 성공")
    void createToken_and_getRole() {
        String token = jwtTokenProvider.createToken(1L, "MERCHANT");

        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("MERCHANT");
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.createToken(1L, "MERCHANT");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰 검증 실패")
    void validateToken_tamperedToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_expiredToken_returnsFalse() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(
                "payment-platform-secret-key-must-be-at-least-256-bits-long",
                -1000L // 이미 만료
        );
        String token = expiredProvider.createToken(1L, "MERCHANT");

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }
}
