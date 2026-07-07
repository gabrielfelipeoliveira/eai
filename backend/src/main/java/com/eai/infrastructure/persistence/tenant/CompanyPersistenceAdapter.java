package com.eai.infrastructure.persistence.tenant;

import com.eai.application.tenant.CompanyRepository;
import com.eai.domain.tenant.Company;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CompanyPersistenceAdapter implements CompanyRepository {

    private final SpringDataCompanyRepository repository;

    public CompanyPersistenceAdapter(SpringDataCompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Company> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Company> findById(UUID id) {
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
    public Company save(Company company) {
        return toDomain(repository.save(toEntity(company)));
    }

    private Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(),
                entity.getName(),
                entity.getDocument(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CompanyJpaEntity toEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.getId());
        entity.setName(company.getName());
        entity.setDocument(company.getDocument());
        entity.setEmail(company.getEmail());
        entity.setPhone(company.getPhone());
        entity.setStatus(company.getStatus());
        entity.setCreatedAt(company.getCreatedAt());
        entity.setUpdatedAt(company.getUpdatedAt());
        return entity;
    }
}
