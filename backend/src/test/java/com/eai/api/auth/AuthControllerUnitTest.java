package com.eai.api.auth;

import com.eai.application.auth.AuthService;
import com.eai.application.auth.AuthTokens;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserService;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import com.eai.infrastructure.config.SecurityRefreshCookieProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerUnitTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private final AuthService authService = mock(AuthService.class);
    private final UserService userService = mock(UserService.class);
    private final AuthController controller = new AuthController(
            authService,
            userService,
            new SecurityRefreshCookieProperties("refresh.custom", true, "Lax")
    );

    AuthControllerUnitTest() {
        ReflectionTestUtils.setField(controller, "refreshTokenTtlHours", 720L);
    }

    @DisplayName("Login retorna access token no corpo e refresh token em cookie httpOnly")
    @Test
    void loginReturnsAccessTokenAndRefreshCookie() {
        when(authService.login("admin@eai.com", "secret")).thenReturn(new AuthTokens("access", "refresh"));

        ResponseEntity<AuthTokenResponse> response = controller.login(new LoginRequest("admin@eai.com", "secret"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access");
        assertThat(response.getBody().refreshToken()).isNull();
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("refresh.custom=refresh")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=Lax");
    }

    @DisplayName("Refresh prioriza token do cookie antes do corpo")
    @Test
    void refreshPrefersCookieToken() {
        when(authService.refresh("cookie-refresh")).thenReturn(new AuthTokens("access-2", "refresh-2"));

        ResponseEntity<AuthTokenResponse> response = controller.refresh("cookie-refresh", new RefreshTokenRequest("body-refresh"));

        verify(authService).refresh("cookie-refresh");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access-2");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("refresh.custom=refresh-2");
    }

    @DisplayName("Refresh usa token do corpo quando cookie nao foi enviado")
    @Test
    void refreshUsesRequestBodyWhenCookieMissing() {
        when(authService.refresh("body-refresh")).thenReturn(new AuthTokens("access-3", "refresh-3"));

        ResponseEntity<AuthTokenResponse> response = controller.refresh(" ", new RefreshTokenRequest("body-refresh"));

        verify(authService).refresh("body-refresh");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
    }

    @DisplayName("Endpoint me carrega usuario autenticado")
    @Test
    void meReturnsAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = authenticatedUser();
        when(userService.getUser(USER_ID)).thenReturn(user());

        assertThat(controller.me(authenticatedUser).id()).isEqualTo(USER_ID);
    }

    @DisplayName("Logout revoga sessoes e expira cookie de refresh")
    @Test
    void logoutRevokesAndExpiresRefreshCookie() {
        ResponseEntity<Void> response = controller.logout(authenticatedUser());

        verify(authService).logout(USER_ID);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("refresh.custom=")
                .contains("Max-Age=0");
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(USER_ID, "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
    }

    private User user() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new User(USER_ID, "Admin", "admin@eai.com", "hash", null, null, null, null, UserStatus.ACTIVE, Set.of(UserRole.ADMIN), now, now);
    }
}
