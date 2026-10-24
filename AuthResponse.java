package com.example.studentmanagement.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs, String email) {
}
