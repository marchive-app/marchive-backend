package com.marchive.marchive_backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marchive.marchive_backend.auth.domain.RefreshToken;
import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.dto.AuthDtos.TokenPairWithUser;
import com.marchive.marchive_backend.auth.dto.AuthDtos.UserDto;
import com.marchive.marchive_backend.auth.repository.RefreshTokenRepository;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.auth.security.DefaultGoogleTokenVerifier;
import com.marchive.marchive_backend.auth.security.JwtProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private DefaultGoogleTokenVerifier defaultGoogleTokenVerifier;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private final String fakeGoogleIdToken = "fake-id-token";
    private final DefaultGoogleTokenVerifier.GoogleUserInfo googleUserInfo =
            new DefaultGoogleTokenVerifier.GoogleUserInfo("google-sub-123", "test@gmail.com", "테스트유저");

    private User createUserWithId(Long id, String googleSub, String email, String nickname) {
        User user = new User(googleSub, email, nickname);
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private void stubGoogleVerifyAndTokenIssue() {
        when(defaultGoogleTokenVerifier.verify(fakeGoogleIdToken)).thenReturn(googleUserInfo);
        when(jwtProvider.createAccessToken(anyLong())).thenReturn("fake-access-token");
        when(jwtProvider.createRefreshToken(anyLong())).thenReturn("fake-refresh-token");
        when(jwtProvider.getRefreshTokenValidMs()).thenReturn(1000L * 60 * 60 * 24 * 14);
    }

    @Test
    void 처음_로그인하는_사용자는_자동으로_회원가입된다() {
        // given: DB에 이 구글 계정으로 가입된 사용자가 아직 없음
        stubGoogleVerifyAndTokenIssue();
        when(userRepository.findByGoogleSub("google-sub-123")).thenReturn(Optional.empty());

        User newUser = createUserWithId(1L, "google-sub-123", "test@gmail.com", "테스트유저");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        TokenPairWithUser result = authService.loginOrSignup(fakeGoogleIdToken);

        // then
        assertThat(result.accessToken()).isEqualTo("fake-access-token");
        assertThat(result.refreshToken()).isEqualTo("fake-refresh-token");
        assertThat(result.user().email()).isEqualTo("test@gmail.com");
        assertThat(result.user().nickname()).isEqualTo("테스트유저");
        verify(userRepository).save(any(User.class)); // 회원가입(save)이 실제로 호출됐는지 확인
        verify(refreshTokenRepository).save(any(RefreshToken.class)); // 토큰도 저장됐는지 확인
    }

    @Test
    void 이미_가입된_사용자는_새로_가입되지_않고_로그인만_된다() {
        // given: 이미 존재하는 사용자
        stubGoogleVerifyAndTokenIssue();
        User existingUser = createUserWithId(1L, "google-sub-123", "test@gmail.com", "테스트유저");
        when(userRepository.findByGoogleSub("google-sub-123")).thenReturn(Optional.of(existingUser));

        // when
        authService.loginOrSignup(fakeGoogleIdToken);

        // then
        verify(userRepository, never()).save(any(User.class)); // save가 호출되면 안 됨(중복가입 방지)
    }

    @Test
    void 로그아웃하면_해당_리프레시_토큰이_삭제된다() {
        // given
        RefreshToken savedToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken("some-refresh-token"))
                .thenReturn(Optional.of(savedToken));

        // when
        authService.logout("some-refresh-token");

        // then
        verify(refreshTokenRepository).delete(savedToken);
    }

    @Test
    void 회원_탈퇴하면_상태가_바뀌고_모든_리프레시_토큰이_삭제된다() {
        // given
        User user = createUserWithId(1L, "google-sub-123", "test@gmail.com", "테스트유저");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        authService.withdraw(1L);

        // then
        assertThat(user.isDeleted()).isTrue();
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void 유저_정보를_조회하면_UserDto를_반환한다() {
        User user = createUserWithId(1L, "google-sub-123", "test@gmail.com", "테스트유저");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = authService.getUserInfo(1L);

        assertThat(result.email()).isEqualTo("test@gmail.com");
        assertThat(result.nickname()).isEqualTo("테스트유저");
    }

    @Test
    void 존재하지_않는_유저_조회시_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserInfo(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}