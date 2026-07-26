package com.marchive.marchive_backend.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtProviderTest {

    // 32바이트 이상의 테스트용 secret
    private final JwtProvider jwtProvider =
            new JwtProvider("test-secret-key-for-jwt-must-be-32-bytes-or-more!!");

    @Test
    void 액세스_토큰을_발급하고_검증하면_같은_userId가_나온다() {
        // given
        Long userId = 1L;

        // when
        String token = jwtProvider.createAccessToken(userId);
        Long extractedUserId = jwtProvider.validateAndGetUserId(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void 리프레시_토큰이_정상적으로_발급되고_검증된다() {
        Long userId = 2L;

        String token = jwtProvider.createRefreshToken(userId);
        Long extractedUserId = jwtProvider.validateAndGetUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void 조작된_토큰은_검증에_실패한다() {
        String token = jwtProvider.createAccessToken(1L);
        String tamperedToken = token.substring(0, token.length() - 5) + "aaaaa"; // 일부러 값 변조

        assertThatThrownBy(() -> jwtProvider.validateAndGetUserId(tamperedToken))
                .isInstanceOf(IllegalArgumentException.class);
    }
}