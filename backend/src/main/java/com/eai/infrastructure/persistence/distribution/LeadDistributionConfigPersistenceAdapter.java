package com.eai.infrastructure.persistence.distribution;

import com.eai.application.distribution.LeadDistributionConfigRepository;
import com.eai.domain.distribution.LeadDistributionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadDistributionConfigPersistenceAdapter implements LeadDistributionConfigRepository {

    private final SpringDataLeadDistributionConfigRepository repository;

    @Override
    public Optional<LeadDistributionConfig> findByCompanyIdAndStoreId(UUID companyId, UUID storeId) {
        return repository.findByCompanyIdAndStoreId(companyId, storeId).map(this::toDomain);
    }

    @Override
    public LeadDistributionConfig save(LeadDistributionConfig config) {
        return toDomain(repository.save(toEntity(config)));
    }

    private LeadDistributionConfig toDomain(LeadDistributionConfigJpaEntity entity) {
        return new LeadDistributionConfig(entity.getId(), entity.getCompanyId(), entity.getStoreId(), entity.getMode(), entity.isActive());
    }

    private LeadDistributionConfigJpaEntity toEntity(LeadDistributionConfig config) {
        LeadDistributionConfigJpaEntity entity = new LeadDistributionConfigJpaEntity();
        entity.setId(config.getId());
        entity.setCompanyId(config.getCompanyId());
        entity.setStoreId(config.getStoreId());
        entity.setMode(config.getMode());
        entity.setActive(config.isActive());
        return entity;
    }
}
