package com.marchive.marchive_backend.auth.controller;

import com.marchive.marchive_backend.auth.dto.AuthDtos.UserResponse;
import com.marchive.marchive_backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;


    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(new UserResponse(authService.getUserInfo(userId)));
    }
}
