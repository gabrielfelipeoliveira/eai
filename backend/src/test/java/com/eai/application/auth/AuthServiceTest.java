package com.eai.application.auth;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.PasswordHasher;
import com.eai.application.security.TokenProvider;
import com.eai.application.user.UserRepository;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Duration REFRESH_TTL = Duration.ofHours(720);

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final AuthService service = new AuthService(
            userRepository,
            refreshTokenRepository,
            passwordHasher,
            tokenProvider,
            REFRESH_TTL.toHours()
    );

    @Test
    void loginRevokesPreviousSessionsBeforeSavingNewRefreshToken() {
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByEmail("admin@eai.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshTokenRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokens tokens = service.login(" Admin@EAI.com ", "secret");

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isNotBlank();
        InOrder inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).revokeAllByUserId(USER_ID);
        inOrder.verify(refreshTokenRepository).save(any(RefreshTokenRecord.class));
    }

    @Test
    void refreshRevokesUsedTokenBeforeSavingNextRefreshToken() {
        User user = user(UserStatus.ACTIVE);
        RefreshTokenRecord currentToken = tokenRecord(false, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(currentToken));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tokenProvider.createAccessToken(user)).thenReturn("next-access-token");
        when(refreshTokenRepository.save(any(RefreshTokenRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokens tokens = service.refresh("refresh-token");

        assertThat(tokens.accessToken()).isEqualTo("next-access-token");
        InOrder inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).revoke(currentToken.id());
        inOrder.verify(refreshTokenRepository).save(any(RefreshTokenRecord.class));
    }

    @Test
    void refreshRejectsExpiredOrRevokedToken() {
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(tokenRecord(true, Instant.now().plusSeconds(60))));

        assertThatThrownBy(() -> service.refresh("revoked-token"))
                .isInstanceOf(UnauthorizedException.class);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(tokenRecord(false, Instant.now().minusSeconds(1))));

        assertThatThrownBy(() -> service.refresh("expired-token"))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository, never()).revoke(any(UUID.class));
        verify(refreshTokenRepository, never()).save(any(RefreshTokenRecord.class));
    }

    @Test
    void inactiveUserCannotLoginOrRefreshSession() {
        User inactiveUser = user(UserStatus.INACTIVE);
        when(userRepository.findByEmail("admin@eai.com")).thenReturn(Optional.of(inactiveUser));
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(tokenRecord(false, Instant.now().plusSeconds(60))));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> service.login("admin@eai.com", "secret"))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.refresh("refresh-token"))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository, never()).revokeAllByUserId(USER_ID);
        verify(refreshTokenRepository, never()).revoke(any(UUID.class));
        verify(refreshTokenRepository, never()).save(any(RefreshTokenRecord.class));
    }

    @Test
    void newRefreshTokenExpiresInThirtyDays() {
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByEmail("admin@eai.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshTokenRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        service.login("admin@eai.com", "secret");

        Instant after = Instant.now();
        ArgumentCaptor<RefreshTokenRecord> tokenCaptor = ArgumentCaptor.forClass(RefreshTokenRecord.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().expiresAt()).isBetween(before.plus(REFRESH_TTL), after.plus(REFRESH_TTL));
    }

    private User user(UserStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new User(
                USER_ID,
                "Admin EAI",
                "admin@eai.com",
                "password-hash",
                null,
                null,
                null,
                null,
                status,
                Set.of(UserRole.ADMIN),
                now,
                now
        );
    }

    private RefreshTokenRecord tokenRecord(boolean revoked, Instant expiresAt) {
        return new RefreshTokenRecord(
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                USER_ID,
                "token-hash",
                expiresAt,
                revoked
        );
    }
}
