package com.eai.infrastructure.persistence.email;

import com.eai.application.email.EmailAccountRepository;
import com.eai.domain.email.EmailAccount;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmailAccountPersistenceAdapter implements EmailAccountRepository {

    private final SpringDataEmailAccountRepository repository;

    public EmailAccountPersistenceAdapter(SpringDataEmailAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EmailAccount> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<EmailAccount> findActive() {
        return repository.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public List<EmailAccount> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<EmailAccount> findByStoreIdIn(List<UUID> storeIds) {
        if (storeIds.isEmpty()) {
            return List.of();
        }
        return repository.findByStoreIdIn(storeIds).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<EmailAccount> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public EmailAccount save(EmailAccount account) {
        return toDomain(repository.save(toEntity(account)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private EmailAccount toDomain(EmailAccountJpaEntity entity) {
        return new EmailAccount(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getName(),
                entity.getHost(),
                entity.getPort(),
                entity.getUsername(),
                entity.getEncryptedPassword(),
                entity.getProtocol(),
                entity.isUseSsl(),
                entity.isActive(),
                entity.getLastReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastSyncStatus(),
                entity.getLastSyncMessage(),
                entity.getLastSyncAt()
        );
    }

    private EmailAccountJpaEntity toEntity(EmailAccount account) {
        EmailAccountJpaEntity entity = new EmailAccountJpaEntity();
        entity.setId(account.getId());
        entity.setCompanyId(account.getCompanyId());
        entity.setStoreId(account.getStoreId());
        entity.setName(account.getName());
        entity.setHost(account.getHost());
        entity.setPort(account.getPort());
        entity.setUsername(account.getUsername());
        entity.setEncryptedPassword(account.getEncryptedPassword());
        entity.setProtocol(account.getProtocol());
        entity.setUseSsl(account.isUseSsl());
        entity.setActive(account.isActive());
        entity.setLastReadAt(account.getLastReadAt());
        entity.setCreatedAt(account.getCreatedAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        entity.setLastSyncStatus(account.getLastSyncStatus());
        entity.setLastSyncMessage(account.getLastSyncMessage());
        entity.setLastSyncAt(account.getLastSyncAt());
        return entity;
    }
}
