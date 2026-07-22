package com.eai.domain.lead;

import com.eai.domain.item.Item;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Lead {

    private final UUID id;
    private UUID companyId;
    private UUID storeId;
    private String customerName;
    private String customerPhone;
    private List<String> additionalPhones;
    private String customerEmail;
    private String customerCity;
    private String vehicleInterest;
    private UUID itemId;
    private Item item;
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
    private String saleCurrency;
    private UUID relatedLeadId;

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
        this(
                id,
                companyId,
                storeId,
                customerName,
                customerPhone,
                List.of(),
                customerEmail,
                customerCity,
                vehicleInterest,
                null,
                null,
                source,
                originalMessage,
                status,
                assignedToUserId,
                assignedAt,
                createdAt,
                updatedAt,
                firstContactAt,
                lastContactAt,
                lostReason,
                saleValue,
                null,
                null
        );
    }

    public Lead(
            UUID id,
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            UUID itemId,
            Item item,
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
            BigDecimal saleValue,
            String saleCurrency
    ) {
        this(
                id,
                companyId,
                storeId,
                customerName,
                customerPhone,
                List.of(),
                customerEmail,
                customerCity,
                vehicleInterest,
                itemId,
                item,
                source,
                originalMessage,
                status,
                assignedToUserId,
                assignedAt,
                createdAt,
                updatedAt,
                firstContactAt,
                lastContactAt,
                lostReason,
                saleValue,
                saleCurrency,
                null
        );
    }

    public Lead(
            UUID id,
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            List<String> additionalPhones,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            UUID itemId,
            Item item,
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
            BigDecimal saleValue,
            String saleCurrency,
            UUID relatedLeadId
    ) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.customerName = requireText(customerName, "customerName");
        this.customerPhone = trimToNull(customerPhone);
        this.additionalPhones = normalizeAdditionalPhones(additionalPhones, this.customerPhone);
        this.customerEmail = trimToNull(customerEmail);
        this.customerCity = trimToNull(customerCity);
        this.vehicleInterest = trimToNull(vehicleInterest);
        this.itemId = itemId;
        this.item = item;
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
        this.saleCurrency = normalizeCurrency(saleCurrency);
        this.relatedLeadId = relatedLeadId;
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
        return create(
                companyId,
                storeId,
                customerName,
                customerPhone,
                List.of(),
                customerEmail,
                customerCity,
                vehicleInterest,
                null,
                null,
                source,
                originalMessage,
                assignedToUserId,
                lostReason,
                saleValue,
                null
        );
    }

    public static Lead create(
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            List<String> additionalPhones,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            UUID itemId,
            Item item,
            LeadSource source,
            String originalMessage,
            UUID assignedToUserId,
            String lostReason,
            BigDecimal saleValue,
            String saleCurrency
    ) {
        Instant now = Instant.now();
        LeadStatus initialStatus = source == LeadSource.MANUAL ? LeadStatus.AVAILABLE : LeadStatus.NEW;
        return new Lead(
                UUID.randomUUID(),
                companyId,
                storeId,
                customerName,
                customerPhone,
                additionalPhones,
                customerEmail,
                customerCity,
                vehicleInterest,
                itemId,
                item,
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
                saleValue,
                saleCurrency,
                null
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
        update(
                companyId,
                storeId,
                customerName,
                customerPhone,
                additionalPhones,
                customerEmail,
                customerCity,
                vehicleInterest,
                itemId,
                item,
                source,
                originalMessage,
                status,
                assignedToUserId,
                assignedAt,
                firstContactAt,
                lastContactAt,
                lostReason,
                saleValue,
                saleCurrency
        );
    }

    public void update(
            UUID companyId,
            UUID storeId,
            String customerName,
            String customerPhone,
            List<String> additionalPhones,
            String customerEmail,
            String customerCity,
            String vehicleInterest,
            UUID itemId,
            Item item,
            LeadSource source,
            String originalMessage,
            LeadStatus status,
            UUID assignedToUserId,
            Instant assignedAt,
            Instant firstContactAt,
            Instant lastContactAt,
            String lostReason,
            BigDecimal saleValue,
            String saleCurrency
    ) {
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.customerName = requireText(customerName, "customerName");
        this.customerPhone = trimToNull(customerPhone);
        this.additionalPhones = normalizeAdditionalPhones(additionalPhones, this.customerPhone);
        this.customerEmail = trimToNull(customerEmail);
        this.customerCity = trimToNull(customerCity);
        this.vehicleInterest = trimToNull(vehicleInterest);
        this.itemId = itemId;
        this.item = item;
        this.source = Objects.requireNonNull(source);
        this.originalMessage = trimToNull(originalMessage);
        this.status = Objects.requireNonNull(status);
        this.assignedToUserId = assignedToUserId;
        this.assignedAt = assignedToUserId == null ? null : assignedAt;
        this.firstContactAt = firstContactAt;
        this.lastContactAt = lastContactAt;
        this.lostReason = trimToNull(lostReason);
        this.saleValue = saleValue;
        this.saleCurrency = normalizeCurrency(saleCurrency);
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
                || newStatus == LeadStatus.VISIT_SCHEDULED || newStatus == LeadStatus.SIMULATING
                || newStatus == LeadStatus.PROPOSAL_APPROVED || newStatus == LeadStatus.PROPOSAL_SENT
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

    public LeadStatus markDuplicated(UUID relatedLeadId) {
        if (relatedLeadId != null && relatedLeadId.equals(this.id)) {
            throw new IllegalArgumentException("relatedLeadId cannot reference the same lead");
        }
        this.relatedLeadId = relatedLeadId;
        return changeStatus(LeadStatus.DUPLICATED);
    }

    public List<String> getAdditionalPhones() {
        return Collections.unmodifiableList(additionalPhones);
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

    private static List<String> normalizeAdditionalPhones(List<String> values, String customerPhone) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String phone = trimToNull(value);
            if (phone == null || phone.equals(customerPhone) || normalized.contains(phone)) {
                continue;
            }
            normalized.add(phone);
        }
        return List.copyOf(normalized);
    }

    private static String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "BRL";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("saleCurrency must have exactly 3 uppercase letters");
        }
        return normalized;
    }
}
