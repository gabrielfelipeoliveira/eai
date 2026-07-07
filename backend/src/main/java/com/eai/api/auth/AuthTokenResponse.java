package com.eai.api.auth;

public record AuthTokenResponse(String accessToken, String refreshToken, String tokenType) {
}
