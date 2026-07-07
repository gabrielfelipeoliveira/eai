package com.eai.api.auth;

import com.eai.api.user.UserResponse;
import com.eai.application.auth.AuthService;
import com.eai.application.auth.AuthTokens;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return toResponse(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return UserResponse.fromDomain(userService.getUser(authenticatedUser.id()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        authService.logout(authenticatedUser.id());
        return ResponseEntity.noContent().build();
    }

    private AuthTokenResponse toResponse(AuthTokens tokens) {
        return new AuthTokenResponse(tokens.accessToken(), tokens.refreshToken(), "Bearer");
    }
}
