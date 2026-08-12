package com.marchive.marchive_backend.auth.dto;

public class AuthDtos {

    public record UserDto(
            String email,
            String nickname
    ) {
    }

    public record TokenPairWithUser(
            String accessToken,
            String refreshToken,
            UserDto user
    ) {
    }

    public record UserResponse(UserDto user) {
    }
}
