package com.marchive.marchive_backend.auth.security;

public interface GoogleTokenVerifier {

    GoogleUserInfo verify(String idTokenString);

    record GoogleUserInfo(String googleSub, String email, String nickname, String nonce) {
    }
}
