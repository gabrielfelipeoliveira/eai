package com.eai.infrastructure.persistence.lead;

import com.eai.application.item.ItemRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.lead.PageResult;
import com.eai.domain.item.Item;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadPersistenceAdapter implements LeadRepository {

    private static final List<LeadStatus> PENDING_STATUSES = List.of(LeadStatus.NEW, LeadStatus.AVAILABLE);
    private static final List<LeadStatus> SELLER_AVAILABLE_STATUSES = List.of(LeadStatus.NEW, LeadStatus.AVAILABLE);
    private static final List<LeadStatus> OPEN_STATUSES = List.of(
            LeadStatus.ASSIGNED,
            LeadStatus.FIRST_CONTACT,
            LeadStatus.IN_NEGOTIATION,
            LeadStatus.VISIT_SCHEDULED,
            LeadStatus.SIMULATING,
            LeadStatus.PROPOSAL_APPROVED,
            LeadStatus.PROPOSAL_SENT
    );
    private static final List<LeadStatus> SLA_CANDIDATE_STATUSES = List.of(
            LeadStatus.NEW,
            LeadStatus.AVAILABLE,
            LeadStatus.ASSIGNED,
            LeadStatus.FIRST_CONTACT,
            LeadStatus.IN_NEGOTIATION,
            LeadStatus.VISIT_SCHEDULED,
            LeadStatus.SIMULATING,
            LeadStatus.PROPOSAL_APPROVED,
            LeadStatus.PROPOSAL_SENT
    );

    private final SpringDataLeadRepository repository;
    private final ItemRepository itemRepository;

    @Override
    public PageResult<Lead> search(LeadSearchCriteria criteria, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
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
    public List<Lead> findAll(LeadSearchCriteria criteria) {
        return repository.findAll(toSpecification(criteria), Sort.by(Sort.Direction.ASC, "createdAt")).stream()
                .map(this::toDomain)
                .toList();
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
    public Optional<Lead> findMostRecentByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
        if (phones == null || phones.isEmpty()) {
            return Optional.empty();
        }
        return repository.findByStoreIdAndAnyPhoneOrderByCreatedAtDesc(storeId, phones).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public long countOpenByAssignedToUserId(UUID userId) {
        return repository.countByAssignedToUserIdAndStatusIn(userId, OPEN_STATUSES);
    }

    @Override
    public boolean existsByStoreIdAndAnyPhone(UUID storeId, List<String> phones) {
        return findMostRecentByStoreIdAndAnyPhone(storeId, phones).isPresent();
    }

    private Specification<LeadJpaEntity> toSpecification(LeadSearchCriteria criteria) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            Join<LeadJpaEntity, String> additionalPhones = null;
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
            if (criteria.visibleToSellerUserId() != null) {
                predicates.add(builder.or(
                        builder.equal(root.get("assignedToUserId"), criteria.visibleToSellerUserId()),
                        builder.and(
                                builder.isNull(root.get("assignedToUserId")),
                                root.get("status").in(SELLER_AVAILABLE_STATUSES)
                        )
                ));
            }
            if (criteria.createdFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), criteria.createdFrom()));
            }
            if (criteria.createdTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), criteria.createdTo()));
            }
            if (criteria.vehicle() != null && !criteria.vehicle().isBlank()) {
                predicates.add(builder.like(normalizedText(builder, root.get("vehicleInterest")), likeNormalized(criteria.vehicle())));
            }
            if (criteria.phone() != null && !criteria.phone().isBlank()) {
                additionalPhones = root.join("additionalPhones", JoinType.LEFT);
                query.distinct(true);
                String phoneValue = likePhone(criteria.phone());
                predicates.add(phoneValue == null
                        ? builder.disjunction()
                        : builder.or(
                                builder.like(normalizedPhone(builder, root.get("customerPhone")), phoneValue),
                                builder.like(normalizedPhone(builder, additionalPhones), phoneValue)
                        ));
            }
            if (criteria.text() != null && !criteria.text().isBlank()) {
                if (additionalPhones == null) {
                    additionalPhones = root.join("additionalPhones", JoinType.LEFT);
                    query.distinct(true);
                }
                String value = likeNormalized(criteria.text());
                String phoneValue = likePhone(criteria.text());
                List<Predicate> textPredicates = new ArrayList<>();
                textPredicates.add(builder.like(normalizedText(builder, root.get("customerName")), value));
                textPredicates.add(builder.like(normalizedText(builder, root.get("customerEmail")), value));
                textPredicates.add(builder.like(normalizedText(builder, root.get("customerCity")), value));
                textPredicates.add(builder.like(normalizedText(builder, root.get("vehicleInterest")), value));
                textPredicates.add(builder.like(normalizedText(builder, root.get("originalMessage")), value));
                if (phoneValue != null) {
                    textPredicates.add(builder.like(normalizedPhone(builder, root.get("customerPhone")), phoneValue));
                    textPredicates.add(builder.like(normalizedPhone(builder, additionalPhones), phoneValue));
                }
                predicates.add(builder.or(
                        textPredicates.toArray(Predicate[]::new)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String likeNormalized(String value) {
        return "%" + normalizeText(value) + "%";
    }

    private String likePhone(String value) {
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : "%" + digits + "%";
    }

    private String normalizeText(String value) {
        String withoutAccents = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents;
    }

    private Expression<String> normalizedText(jakarta.persistence.criteria.CriteriaBuilder builder, Expression<String> expression) {
        Expression<String> normalized = builder.lower(builder.coalesce(expression, ""));
        normalized = replace(builder, normalized, "á", "a");
        normalized = replace(builder, normalized, "à", "a");
        normalized = replace(builder, normalized, "ã", "a");
        normalized = replace(builder, normalized, "â", "a");
        normalized = replace(builder, normalized, "ä", "a");
        normalized = replace(builder, normalized, "é", "e");
        normalized = replace(builder, normalized, "è", "e");
        normalized = replace(builder, normalized, "ê", "e");
        normalized = replace(builder, normalized, "ë", "e");
        normalized = replace(builder, normalized, "í", "i");
        normalized = replace(builder, normalized, "ì", "i");
        normalized = replace(builder, normalized, "î", "i");
        normalized = replace(builder, normalized, "ï", "i");
        normalized = replace(builder, normalized, "ó", "o");
        normalized = replace(builder, normalized, "ò", "o");
        normalized = replace(builder, normalized, "õ", "o");
        normalized = replace(builder, normalized, "ô", "o");
        normalized = replace(builder, normalized, "ö", "o");
        normalized = replace(builder, normalized, "ú", "u");
        normalized = replace(builder, normalized, "ù", "u");
        normalized = replace(builder, normalized, "û", "u");
        normalized = replace(builder, normalized, "ü", "u");
        normalized = replace(builder, normalized, "ç", "c");
        return normalized;
    }

    private Expression<String> normalizedPhone(jakarta.persistence.criteria.CriteriaBuilder builder, Expression<String> expression) {
        Expression<String> normalized = builder.coalesce(expression, "");
        normalized = replace(builder, normalized, "+", "");
        normalized = replace(builder, normalized, " ", "");
        normalized = replace(builder, normalized, "-", "");
        normalized = replace(builder, normalized, "(", "");
        normalized = replace(builder, normalized, ")", "");
        normalized = replace(builder, normalized, ".", "");
        return normalized;
    }

    private Expression<String> replace(jakarta.persistence.criteria.CriteriaBuilder builder, Expression<String> expression, String target, String replacement) {
        return builder.function("replace", String.class, expression, builder.literal(target), builder.literal(replacement));
    }

    private Lead toDomain(LeadJpaEntity entity) {
        Item item = null;
        if (entity.getItemId() != null) {
            ItemRepository.ItemWithVehicle itemWithVehicle = itemRepository.findById(entity.getItemId()).orElse(null);
            if (itemWithVehicle != null) {
                item = itemWithVehicle.item();
            }
        }
        return new Lead(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getCustomerName(),
                entity.getCustomerPhone(),
                List.copyOf(entity.getAdditionalPhones()),
                entity.getCustomerEmail(),
                entity.getCustomerCity(),
                entity.getVehicleInterest(),
                entity.getItemId(),
                item,
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
                entity.getSaleValue(),
                entity.getSaleCurrency(),
                entity.getRelatedLeadId()
        );
    }

    private LeadJpaEntity toEntity(Lead lead) {
        LeadJpaEntity entity = new LeadJpaEntity();
        entity.setId(lead.getId());
        entity.setCompanyId(lead.getCompanyId());
        entity.setStoreId(lead.getStoreId());
        entity.setCustomerName(lead.getCustomerName());
        entity.setCustomerPhone(lead.getCustomerPhone());
        entity.setAdditionalPhones(new java.util.HashSet<>(lead.getAdditionalPhones()));
        entity.setCustomerEmail(lead.getCustomerEmail());
        entity.setCustomerCity(lead.getCustomerCity());
        entity.setVehicleInterest(lead.getVehicleInterest());
        entity.setItemId(lead.getItemId());
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
        entity.setSaleCurrency(lead.getSaleCurrency());
        entity.setRelatedLeadId(lead.getRelatedLeadId());
        return entity;
    }
}
