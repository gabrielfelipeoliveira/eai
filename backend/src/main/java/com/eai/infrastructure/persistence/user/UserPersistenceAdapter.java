package com.eai.infrastructure.persistence.user;

import com.eai.application.user.UserRepository;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<User> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<User> findByStoreId(UUID storeId) {
        return repository.findByStoreId(storeId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByCompanyId(UUID companyId) {
        return repository.existsByCompanyIdAndStatus(companyId, UserStatus.ACTIVE);
    }

    @Override
    public List<User> findActiveByStoreId(UUID storeId) {
        return repository.findByStoreIdAndStatus(storeId, UserStatus.ACTIVE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<User> findActiveByRole(UserRole role) {
        return repository.findByStatusAndRole(UserStatus.ACTIVE, role).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return repository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getPhone(),
                entity.getJobTitle(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getStatus(),
                entity.getRoles(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPhone(user.getPhone());
        entity.setJobTitle(user.getJobTitle());
        entity.setCompanyId(user.getCompanyId());
        entity.setStoreId(user.getStoreId());
        entity.setStatus(user.getStatus());
        entity.setRoles(user.getRoles());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
