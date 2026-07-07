package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.lead.PageResult;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class LeadPersistenceAdapter implements LeadRepository {

    private static final List<LeadStatus> PENDING_STATUSES = List.of(LeadStatus.NEW, LeadStatus.AVAILABLE);
    private static final List<LeadStatus> OPEN_STATUSES = List.of(
            LeadStatus.ASSIGNED,
            LeadStatus.FIRST_CONTACT,
            LeadStatus.IN_NEGOTIATION,
            LeadStatus.VISIT_SCHEDULED,
            LeadStatus.PROPOSAL_SENT
    );
    private static final List<LeadStatus> SLA_CANDIDATE_STATUSES = List.of(
            LeadStatus.NEW,
            LeadStatus.AVAILABLE,
            LeadStatus.ASSIGNED,
            LeadStatus.FIRST_CONTACT,
            LeadStatus.IN_NEGOTIATION,
            LeadStatus.VISIT_SCHEDULED,
            LeadStatus.PROPOSAL_SENT
    );

    private final SpringDataLeadRepository repository;

    public LeadPersistenceAdapter(SpringDataLeadRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Lead> search(LeadSearchCriteria criteria, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findAll(toSpecification(criteria), pageable);
        return new PageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Optional<Lead> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Lead save(Lead lead) {
        return toDomain(repository.save(toEntity(lead)));
    }

    @Override
    public List<Lead> findPendingByStoreId(UUID storeId) {
        return repository.findByStoreIdAndAssignedToUserIdIsNullAndStatusInOrderByCreatedAtAsc(storeId, PENDING_STATUSES).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Lead> findOverdueCandidatesByStoreId(UUID storeId) {
        return repository.findByStoreIdAndStatusInOrderByCreatedAtAsc(storeId, SLA_CANDIDATE_STATUSES).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UUID> findMostRecentAssignedSellerId(UUID storeId) {
        return repository.findTopByStoreIdAndAssignedToUserIdIsNotNullOrderByAssignedAtDescUpdatedAtDesc(storeId)
                .map(LeadJpaEntity::getAssignedToUserId);
    }

    @Override
    public long countOpenByAssignedToUserId(UUID userId) {
        return repository.countByAssignedToUserIdAndStatusIn(userId, OPEN_STATUSES);
    }

    @Override
    public boolean existsByStoreIdAndPhoneAndVehicleSince(UUID storeId, String phone, String vehicleInterest, Instant since) {
        return repository.existsDuplicate(storeId, phone, vehicleInterest.toLowerCase(Locale.ROOT), since);
    }

    private Specification<LeadJpaEntity> toSpecification(LeadSearchCriteria criteria) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (criteria.scopeCompanyId() != null) {
                predicates.add(builder.equal(root.get("companyId"), criteria.scopeCompanyId()));
            }
            if (criteria.scopeStoreId() != null) {
                predicates.add(builder.equal(root.get("storeId"), criteria.scopeStoreId()));
            } else if (criteria.storeId() != null) {
                predicates.add(builder.equal(root.get("storeId"), criteria.storeId()));
            }
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.source() != null) {
                predicates.add(builder.equal(root.get("source"), criteria.source()));
            }
            if (criteria.assignedToUserId() != null) {
                predicates.add(builder.equal(root.get("assignedToUserId"), criteria.assignedToUserId()));
            }
            if (criteria.createdFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), criteria.createdFrom()));
            }
            if (criteria.createdTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), criteria.createdTo()));
            }
            if (criteria.vehicle() != null && !criteria.vehicle().isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("vehicleInterest")), like(criteria.vehicle())));
            }
            if (criteria.phone() != null && !criteria.phone().isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("customerPhone")), like(criteria.phone())));
            }
            if (criteria.text() != null && !criteria.text().isBlank()) {
                String value = like(criteria.text());
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("customerName")), value),
                        builder.like(builder.lower(root.get("customerEmail")), value),
                        builder.like(builder.lower(root.get("customerCity")), value),
                        builder.like(builder.lower(root.get("vehicleInterest")), value),
                        builder.like(builder.lower(root.get("customerPhone")), value),
                        builder.like(builder.lower(root.get("originalMessage")), value)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private Lead toDomain(LeadJpaEntity entity) {
        return new Lead(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getCustomerName(),
                entity.getCustomerPhone(),
                entity.getCustomerEmail(),
                entity.getCustomerCity(),
                entity.getVehicleInterest(),
                entity.getSource(),
                entity.getOriginalMessage(),
                entity.getStatus(),
                entity.getAssignedToUserId(),
                entity.getAssignedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFirstContactAt(),
                entity.getLastContactAt(),
                entity.getLostReason(),
                entity.getSaleValue()
        );
    }

    private LeadJpaEntity toEntity(Lead lead) {
        LeadJpaEntity entity = new LeadJpaEntity();
        entity.setId(lead.getId());
        entity.setCompanyId(lead.getCompanyId());
        entity.setStoreId(lead.getStoreId());
        entity.setCustomerName(lead.getCustomerName());
        entity.setCustomerPhone(lead.getCustomerPhone());
        entity.setCustomerEmail(lead.getCustomerEmail());
        entity.setCustomerCity(lead.getCustomerCity());
        entity.setVehicleInterest(lead.getVehicleInterest());
        entity.setSource(lead.getSource());
        entity.setOriginalMessage(lead.getOriginalMessage());
        entity.setStatus(lead.getStatus());
        entity.setAssignedToUserId(lead.getAssignedToUserId());
        entity.setAssignedAt(lead.getAssignedAt());
        entity.setCreatedAt(lead.getCreatedAt());
        entity.setUpdatedAt(lead.getUpdatedAt());
        entity.setFirstContactAt(lead.getFirstContactAt());
        entity.setLastContactAt(lead.getLastContactAt());
        entity.setLostReason(lead.getLostReason());
        entity.setSaleValue(lead.getSaleValue());
        return entity;
    }
}
