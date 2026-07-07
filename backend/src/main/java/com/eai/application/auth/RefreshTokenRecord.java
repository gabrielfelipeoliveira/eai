package com.eai.application.auth;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenRecord(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked) {
}
