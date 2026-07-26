package com.marchive.marchive_backend.auth.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockGoogleTokenVerifier implements GoogleTokenVerifier {

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        return new GoogleUserInfo(
                "local-test-google-sub-12345",
                "testuser@marchive.com",
                "우리는테스트유저"
        );
    }
}
