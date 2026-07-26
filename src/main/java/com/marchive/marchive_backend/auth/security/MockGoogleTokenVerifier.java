package com.marchive.marchive_backend.auth.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockGoogleTokenVerifier implements GoogleTokenVerifier {

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        return new GoogleUserInfo(
                "local-test-google-sub-12345", // 고정 google_sub
                "testuser@marchive.com",        // 고정 email
                "우리는테스트유저"                   // 고정 nickname
        );
    }
}
