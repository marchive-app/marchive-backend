package com.marchive.marchive_backend.auth.service;

import com.marchive.marchive_backend.auth.domain.RefreshToken;
import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.repository.RefreshTokenRepository;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.auth.security.DefaultGoogleTokenVerifier;
import com.marchive.marchive_backend.auth.security.JwtProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DefaultGoogleTokenVerifier defaultGoogleTokenVerifier;
    private final JwtProvider jwtProvider;


    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            DefaultGoogleTokenVerifier defaultGoogleTokenVerifier,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.defaultGoogleTokenVerifier = defaultGoogleTokenVerifier;
        this.jwtProvider = jwtProvider;
    }

    // API 1. 구글 로그인 / 회원가입
    @Transactional
    public TokenPair loginOrSignup(String googleIdToken) {
        DefaultGoogleTokenVerifier.GoogleUserInfo googleUser = defaultGoogleTokenVerifier.verify(googleIdToken);

        User user = userRepository.findByGoogleSub(googleUser.googleSub())
                .orElseGet(() -> userRepository.save(
                        new User(googleUser.googleSub(), googleUser.email(), googleUser.name())
                ));

        return issueTokens(user);
    }

    // API 2. Access Token 재발급
    @Transactional
    public TokenPair refresh(String refreshTokenValue) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 refresh token입니다."));

        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken);
            throw new IllegalArgumentException("만료된 refresh token입니다.");
        }

        User user = savedToken.getUser();

        // 기존 토큰 삭제 후 새로 발급
        refreshTokenRepository.delete(savedToken);

        return issueTokens(user);
    }

    // API 3. 로그아웃
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    // API 4. 회원 탈퇴
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.delete();
        refreshTokenRepository.deleteByUser(user);
    }

    private TokenPair issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getUserId());

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenValidMs() / 1000);

        refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue, expiresAt));

        return new TokenPair(accessToken, refreshTokenValue);
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
