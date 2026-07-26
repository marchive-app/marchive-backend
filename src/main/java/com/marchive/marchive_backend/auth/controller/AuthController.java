package com.marchive.marchive_backend.auth.controller;

import com.marchive.marchive_backend.auth.service.AuthService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public AuthService.TokenPair googleLogin(@RequestBody GoogleLoginRequest request) {
        return authService.loginOrSignup(request.idToken());
    }

    @PostMapping("/refresh")
    public AuthService.TokenPair refresh(@RequestBody TokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestBody TokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @DeleteMapping("/withdraw")
    public void withdraw(@RequestParam Long userId) {
        authService.withdraw(userId);
    }

    public record GoogleLoginRequest(String idToken) {
    }

    public record TokenRequest(String refreshToken) {
    }
}
