package com.eai.application.auth;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.PasswordHasher;
import com.eai.application.security.TokenProvider;
import com.eai.application.user.UserRepository;
import com.eai.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final Duration refreshTokenTtl;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            @Value("${eai.security.refresh-token-ttl-hours:168}") long refreshTokenTtlHours
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenTtl = Duration.ofHours(refreshTokenTtlHours);
    }

    @Transactional
    public AuthTokens login(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!user.isActive() || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        RefreshTokenRecord currentToken = refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (currentToken.revoked() || currentToken.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findById(currentToken.userId())
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        refreshTokenRepository.revoke(currentToken.id());
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private AuthTokens issueTokens(User user) {
        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        refreshTokenRepository.save(new RefreshTokenRecord(
                UUID.randomUUID(),
                user.getId(),
                hashToken(refreshToken),
                Instant.now().plus(refreshTokenTtl),
                false
        ));
        return new AuthTokens(tokenProvider.createAccessToken(user), refreshToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
