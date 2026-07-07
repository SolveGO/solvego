package com.kdh.solvego.global.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttest";

    @Test
    @DisplayName("JWT 토큰을 생성하고 검증할 수 있다")
    void create_and_validate_token() {
        // given
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 600000L);

        // when
        String token = jwtTokenProvider.createToken(1L);

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("JWT 토큰에서 userId를 추출할 수 있다")
    void get_user_id_from_token() {
        // given
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 600000L);
        String token = jwtTokenProvider.createToken(1L);

        // when
        Long userId = jwtTokenProvider.getUserId(token);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("잘못된 JWT 토큰이면 검증에 실패한다")
    void validate_token_returns_false_when_token_is_invalid() {
        // given
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 600000L);

        // when
        boolean valid = jwtTokenProvider.validateToken("invalid-token");

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("다른 secret으로 서명된 JWT 토큰이면 검증에 실패한다")
    void validate_token_returns_false_when_token_is_signed_with_different_secret() {
        // given
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 600000L);

        JwtTokenProvider otherJwtTokenProvider = new JwtTokenProvider(
                "differentdifferentdifferentdifferentdifferentdifferentdifferent",
                600000L
        );

        String token = otherJwtTokenProvider.createToken(1L);

        // when
        boolean valid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("만료된 JWT 토큰이면 검증에 실패한다")
    void validate_token_returns_false_when_token_is_expired() {
        // given
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, -60000L);
        String expiredToken = jwtTokenProvider.createToken(1L);

        // when
        boolean valid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(valid).isFalse();
    }
}