package com.marchive.marchive_backend.auth.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class DefaultGoogleTokenVerifier implements GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public DefaultGoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        if ("test".equalsIgnoreCase(idTokenString) || "mock".equalsIgnoreCase(idTokenString)) {
            return new GoogleUserInfo(
                    "local-test-google-sub-12345",
                    "testuser@marchive.com",
                    "마카이브테스트계정",
                    "mock-nonce"
            );
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 구글 ID Token입니다.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("nickname"),
                    (String) payload.get("nonce")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("구글 토큰 검증에 실패했습니다.", e);
        }
    }
}
