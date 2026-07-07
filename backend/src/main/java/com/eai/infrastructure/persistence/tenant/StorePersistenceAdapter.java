package com.eai.infrastructure.persistence.tenant;

import com.eai.application.tenant.StoreRepository;
import com.eai.domain.tenant.Store;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StorePersistenceAdapter implements StoreRepository {

    private final SpringDataStoreRepository repository;

    public StorePersistenceAdapter(SpringDataStoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Store> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Store> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Store> findByIdIn(List<UUID> ids) {
        return repository.findByIdIn(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Store> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document);
    }

    @Override
    public boolean existsByDocumentAndIdNot(String document, UUID id) {
        return repository.existsByDocumentAndIdNot(document, id);
    }

    @Override
    public Store save(Store store) {
        return toDomain(repository.save(toEntity(store)));
    }

    private Store toDomain(StoreJpaEntity entity) {
        return new Store(
                entity.getId(),
                entity.getCompanyId(),
                entity.getName(),
                entity.getDocument(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCity(),
                entity.getState(),
                entity.getAddress(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private StoreJpaEntity toEntity(Store store) {
        StoreJpaEntity entity = new StoreJpaEntity();
        entity.setId(store.getId());
        entity.setCompanyId(store.getCompanyId());
        entity.setName(store.getName());
        entity.setDocument(store.getDocument());
        entity.setEmail(store.getEmail());
        entity.setPhone(store.getPhone());
        entity.setCity(store.getCity());
        entity.setState(store.getState());
        entity.setAddress(store.getAddress());
        entity.setStatus(store.getStatus());
        entity.setCreatedAt(store.getCreatedAt());
        entity.setUpdatedAt(store.getUpdatedAt());
        return entity;
    }
}
