package com.eai.application.auth;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshTokenRecord save(RefreshTokenRecord refreshToken);

    Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);

    void revoke(UUID id);

    void revokeAllByUserId(UUID userId);
}
