package com.eai.infrastructure.persistence.auth;

import com.eai.application.auth.RefreshTokenRecord;
import com.eai.application.auth.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repository;

    public RefreshTokenPersistenceAdapter(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord refreshToken) {
        return toRecord(repository.save(toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toRecord);
    }

    @Override
    public void revoke(UUID id) {
        repository.findById(id).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        repository.revokeAllByUserId(userId);
    }

    private RefreshTokenRecord toRecord(RefreshTokenJpaEntity entity) {
        return new RefreshTokenRecord(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    private RefreshTokenJpaEntity toEntity(RefreshTokenRecord record) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(record.id());
        entity.setUserId(record.userId());
        entity.setTokenHash(record.tokenHash());
        entity.setExpiresAt(record.expiresAt());
        entity.setRevoked(record.revoked());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
