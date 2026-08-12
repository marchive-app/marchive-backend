package com.marchive.marchive_backend.auth.service;

import com.marchive.marchive_backend.auth.controller.AuthController.GoogleLoginRequest;
import com.marchive.marchive_backend.auth.domain.RefreshToken;
import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.dto.AuthDtos.TokenPairWithUser;
import com.marchive.marchive_backend.auth.dto.AuthDtos.UserDto;
import com.marchive.marchive_backend.auth.repository.RefreshTokenRepository;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.auth.security.GoogleTokenVerifier;
import com.marchive.marchive_backend.auth.security.JwtProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            GoogleTokenVerifier googleTokenVerifier,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtProvider = jwtProvider;
    }

    // API 1. 구글 로그인 / 회원가입
    @Transactional
    public TokenPairWithUser loginOrSignup(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(request.idToken());

        validateNonce(request, googleUser);

        User user = userRepository.findByGoogleSub(googleUser.googleSub())
                .orElseGet(() -> userRepository.save(
                        new User(googleUser.googleSub(), googleUser.email(), googleUser.nickname())
                ));

        return issueTokens(user);
    }

    // API 2. Access Token 재발급
    @Transactional
    public TokenPairWithUser refresh(String refreshTokenValue) {
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

    // API 5. 유저 정보 조회
    @Transactional(readOnly = true)
    public UserDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return toUserDto(user);
    }

    private TokenPairWithUser issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getUserId());

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenValidMs() / 1000);

        refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue, expiresAt));

        return new TokenPairWithUser(accessToken, refreshTokenValue, toUserDto(user));
    }

    private void validateNonce(GoogleLoginRequest request, GoogleTokenVerifier.GoogleUserInfo googleUser) {
        if (!"extension".equals(request.platform())) {
            return;
        }

        String requestNonce = request.nonce();
        String tokenNonce = googleUser.nonce();

        if (requestNonce == null || requestNonce.isBlank()) {
            throw new IllegalArgumentException("nonce가 누락되었습니다.");
        }

        if (!requestNonce.equals(tokenNonce)) {
            throw new IllegalArgumentException("nonce가 일치하지 않습니다. 유효하지 않은 요청입니다.");
        }
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.getEmail(), user.getNickname());
    }
}
