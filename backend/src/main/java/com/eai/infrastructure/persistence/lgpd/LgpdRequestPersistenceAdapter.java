package com.eai.infrastructure.persistence.lgpd;

import com.eai.application.lgpd.LgpdRequestPageResult;
import com.eai.application.lgpd.LgpdRequestRepository;
import com.eai.application.lgpd.LgpdRequestSearchCriteria;
import com.eai.domain.lgpd.LgpdRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
public class LgpdRequestPersistenceAdapter implements LgpdRequestRepository {

    private final SpringDataLgpdRequestRepository repository;

    public LgpdRequestPersistenceAdapter(SpringDataLgpdRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public LgpdRequestPageResult<LgpdRequest> search(LgpdRequestSearchCriteria criteria, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findAll(toSpecification(criteria), pageable);
        return new LgpdRequestPageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Optional<LgpdRequest> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public LgpdRequest save(LgpdRequest request) {
        return toDomain(repository.save(toEntity(request)));
    }

    private Specification<LgpdRequestJpaEntity> toSpecification(LgpdRequestSearchCriteria criteria) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.companyId() != null) {
                predicates.add(builder.equal(root.get("companyId"), criteria.companyId()));
            }
            if (criteria.storeId() != null) {
                predicates.add(builder.equal(root.get("storeId"), criteria.storeId()));
            }
            if (criteria.leadId() != null) {
                predicates.add(builder.equal(root.get("leadId"), criteria.leadId()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private LgpdRequest toDomain(LgpdRequestJpaEntity entity) {
        return new LgpdRequest(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getLeadId(),
                entity.getDataSubjectName(),
                entity.getDataSubjectPhone(),
                entity.getDataSubjectEmail(),
                entity.getRequestType(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getRequestedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt()
        );
    }

    private LgpdRequestJpaEntity toEntity(LgpdRequest request) {
        LgpdRequestJpaEntity entity = new LgpdRequestJpaEntity();
        entity.setId(request.getId());
        entity.setCompanyId(request.getCompanyId());
        entity.setStoreId(request.getStoreId());
        entity.setLeadId(request.getLeadId());
        entity.setDataSubjectName(request.getDataSubjectName());
        entity.setDataSubjectPhone(request.getDataSubjectPhone());
        entity.setDataSubjectEmail(request.getDataSubjectEmail());
        entity.setRequestType(request.getRequestType());
        entity.setStatus(request.getStatus());
        entity.setDescription(request.getDescription());
        entity.setRequestedByUserId(request.getRequestedByUserId());
        entity.setCreatedAt(request.getCreatedAt());
        entity.setUpdatedAt(request.getUpdatedAt());
        entity.setCompletedAt(request.getCompletedAt());
        return entity;
    }
}
