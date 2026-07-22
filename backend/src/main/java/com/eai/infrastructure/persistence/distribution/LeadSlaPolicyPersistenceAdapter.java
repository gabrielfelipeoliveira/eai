package com.eai.infrastructure.persistence.distribution;

import com.eai.application.distribution.LeadSlaPolicyRepository;
import com.eai.domain.distribution.LeadSlaPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadSlaPolicyPersistenceAdapter implements LeadSlaPolicyRepository {

    private final SpringDataLeadSlaPolicyRepository repository;

    @Override
    public Optional<LeadSlaPolicy> findByCompanyIdAndStoreId(UUID companyId, UUID storeId) {
        return repository.findByCompanyIdAndStoreId(companyId, storeId).map(this::toDomain);
    }

    @Override
    public LeadSlaPolicy save(LeadSlaPolicy policy) {
        return toDomain(repository.save(toEntity(policy)));
    }

    private LeadSlaPolicy toDomain(LeadSlaPolicyJpaEntity entity) {
        return new LeadSlaPolicy(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getMinutesToAssign(),
                entity.getMinutesToFirstContact(),
                entity.isActive()
        );
    }

    private LeadSlaPolicyJpaEntity toEntity(LeadSlaPolicy policy) {
        LeadSlaPolicyJpaEntity entity = new LeadSlaPolicyJpaEntity();
        entity.setId(policy.getId());
        entity.setCompanyId(policy.getCompanyId());
        entity.setStoreId(policy.getStoreId());
        entity.setMinutesToAssign(policy.getMinutesToAssign());
        entity.setMinutesToFirstContact(policy.getMinutesToFirstContact());
        entity.setActive(policy.isActive());
        return entity;
    }
}
