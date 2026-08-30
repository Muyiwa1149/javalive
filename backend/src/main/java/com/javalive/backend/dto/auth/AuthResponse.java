package com.javalive.backend.dto.auth;

public record AuthResponse(String token, UserSummary user) {
}
