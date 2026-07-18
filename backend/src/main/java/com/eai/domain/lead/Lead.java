package com.eai.domain.lead;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Lead {

    private final UUID id;
    private UUID companyId;
    private UUID storeId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerCity;
    private String vehicleInterest;
    private LeadSource source;
    private String originalMessage;
    private LeadStatus status;
    private UUID assignedToUserId;
    private Instant assignedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant firstContactAt;
    private Instant lastContactAt;
    private String lostReason;
    private BigDecimal saleValue;

    public Lead(
            UUID id,
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            LeadSource source,
            String originalMessage,
            LeadStatus status,
            UUID assignedToUserId,
            Instant assignedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant firstContactAt,
            Instant lastContactAt,
            String lostReason,
            BigDecimal saleValue
    ) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.customerName = requireText(customerName, "customerName");
        this.customerPhone = trimToNull(customerPhone);
        this.customerEmail = trimToNull(customerEmail);
        this.customerCity = trimToNull(customerCity);
        this.vehicleInterest = trimToNull(vehicleInterest);
        this.source = Objects.requireNonNull(source);
        this.originalMessage = trimToNull(originalMessage);
        this.status = Objects.requireNonNull(status);
        this.assignedToUserId = assignedToUserId;
        this.assignedAt = assignedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.firstContactAt = firstContactAt;
        this.lastContactAt = lastContactAt;
        this.lostReason = trimToNull(lostReason);
        this.saleValue = saleValue;
    }

    public static Lead create(
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            LeadSource source,
            String originalMessage,
            UUID assignedToUserId,
            String lostReason,
            BigDecimal saleValue
    ) {
        Instant now = Instant.now();
        LeadStatus initialStatus = source == LeadSource.MANUAL ? LeadStatus.AVAILABLE : LeadStatus.NEW;
        return new Lead(
                UUID.randomUUID(),
                companyId,
                storeId,
                customerName,
                customerPhone,
                customerEmail,
                customerCity,
                vehicleInterest,
                source,
                originalMessage,
                initialStatus == LeadStatus.NEW && assignedToUserId != null ? LeadStatus.ASSIGNED : initialStatus,
                assignedToUserId,
                assignedToUserId == null ? null : now,
                now,
                now,
                null,
                null,
                lostReason,
                saleValue
        );
    }

    public void update(
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            LeadSource source,
            String originalMessage,
            LeadStatus status,
            UUID assignedToUserId,
            Instant assignedAt,
            Instant firstContactAt,
            Instant lastContactAt,
            String lostReason,
            BigDecimal saleValue
    ) {
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.customerName = requireText(customerName, "customerName");
        this.customerPhone = trimToNull(customerPhone);
        this.customerEmail = trimToNull(customerEmail);
        this.customerCity = trimToNull(customerCity);
        this.vehicleInterest = trimToNull(vehicleInterest);
        this.source = Objects.requireNonNull(source);
        this.originalMessage = trimToNull(originalMessage);
        this.status = Objects.requireNonNull(status);
        this.assignedToUserId = assignedToUserId;
        this.assignedAt = assignedToUserId == null ? null : assignedAt;
        this.firstContactAt = firstContactAt;
        this.lastContactAt = lastContactAt;
        this.lostReason = trimToNull(lostReason);
        this.saleValue = saleValue;
        this.updatedAt = Instant.now();
    }

    public LeadStatus changeStatus(LeadStatus newStatus) {
        LeadStatus previousStatus = this.status;
        this.status = Objects.requireNonNull(newStatus);
        Instant now = Instant.now();
        if (newStatus == LeadStatus.FIRST_CONTACT && this.firstContactAt == null) {
            this.firstContactAt = now;
        }
        if (newStatus == LeadStatus.FIRST_CONTACT || newStatus == LeadStatus.IN_NEGOTIATION
                || newStatus == LeadStatus.VISIT_SCHEDULED || newStatus == LeadStatus.PROPOSAL_SENT
                || newStatus == LeadStatus.SOLD || newStatus == LeadStatus.LOST) {
            this.lastContactAt = now;
        }
        this.updatedAt = now;
        return previousStatus;
    }

    public LeadStatus assignTo(UUID userId) {
        this.assignedToUserId = Objects.requireNonNull(userId);
        this.assignedAt = Instant.now();
        return changeStatus(LeadStatus.ASSIGNED);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
