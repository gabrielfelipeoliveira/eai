package com.eai.infrastructure.persistence.tenant;

import com.eai.application.tenant.CompanyRepository;
import com.eai.domain.tenant.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyPersistenceAdapter implements CompanyRepository {

    private final SpringDataCompanyRepository repository;

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
    public Company save(Company company) {
        return toDomain(repository.save(toEntity(company)));
    }

    private Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CompanyJpaEntity toEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.getId());
        entity.setName(company.getName());
        entity.setStatus(company.getStatus());
        entity.setCreatedAt(company.getCreatedAt());
        entity.setUpdatedAt(company.getUpdatedAt());
        return entity;
    }
}
