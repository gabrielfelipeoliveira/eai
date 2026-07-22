package com.eai.api.auth;

import com.eai.api.user.UserResponse;
import com.eai.application.auth.AuthService;
import com.eai.application.auth.AuthTokens;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserService;
import com.eai.infrastructure.config.SecurityRefreshCookieProperties;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SecurityRefreshCookieProperties refreshCookieProperties;

    @Value("${eai.security.refresh-token-ttl-hours:720}")
    private long refreshTokenTtlHours;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request.email(), request.password());
        return withRefreshCookie(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
            @CookieValue(name = "${eai.security.refresh-cookie.name:eai.refreshToken}", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        AuthTokens tokens = authService.refresh(resolveRefreshToken(cookieRefreshToken, request));
        return withRefreshCookie(tokens);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return UserResponse.fromDomain(userService.getUser(authenticatedUser.id()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        authService.logout(authenticatedUser.id());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .build();
    }

    private ResponseEntity<AuthTokenResponse> withRefreshCookie(AuthTokens tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken()).toString())
                .body(new AuthTokenResponse(tokens.accessToken(), null, "Bearer"));
    }

    private String resolveRefreshToken(String cookieRefreshToken, RefreshTokenRequest request) {
        if (cookieRefreshToken != null && !cookieRefreshToken.isBlank()) {
            return cookieRefreshToken;
        }
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        return "";
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshCookieProperties.effectiveName(), refreshToken)
                .httpOnly(true)
                .secure(refreshCookieProperties.effectiveSecure())
                .sameSite(refreshCookieProperties.effectiveSameSite())
                .path("/api/auth")
                .maxAge(refreshTokenTtlHours * 3600)
                .build();
    }

    private ResponseCookie expiredRefreshCookie() {
        return ResponseCookie.from(refreshCookieProperties.effectiveName(), "")
                .httpOnly(true)
                .secure(refreshCookieProperties.effectiveSecure())
                .sameSite(refreshCookieProperties.effectiveSameSite())
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
