package com.eai.api.lead;

import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record LeadResponse(
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
        BigDecimal saleValue,
        boolean overdueToAssign,
        boolean overdueToFirstContact
) {

    public static LeadResponse fromDomain(Lead lead) {
        return fromDomain(lead, null, null, Instant.now());
    }

    public static LeadResponse fromDomain(Lead lead, Integer minutesToAssign, Integer minutesToFirstContact, Instant now) {
        boolean overdueToAssign = minutesToAssign != null
                && lead.getAssignedToUserId() == null
                && Duration.between(lead.getCreatedAt(), now).toMinutes() > minutesToAssign;
        Instant firstContactStart = lead.getAssignedAt() == null ? lead.getCreatedAt() : lead.getAssignedAt();
        boolean overdueToFirstContact = minutesToFirstContact != null
                && lead.getAssignedToUserId() != null
                && lead.getFirstContactAt() == null
                && Duration.between(firstContactStart, now).toMinutes() > minutesToFirstContact;
        return new LeadResponse(
                lead.getId(),
                lead.getCompanyId(),
                lead.getStoreId(),
                lead.getCustomerName(),
                lead.getCustomerPhone(),
                lead.getCustomerEmail(),
                lead.getCustomerCity(),
                lead.getVehicleInterest(),
                lead.getSource(),
                lead.getOriginalMessage(),
                lead.getStatus(),
                lead.getAssignedToUserId(),
                lead.getAssignedAt(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getFirstContactAt(),
                lead.getLastContactAt(),
                lead.getLostReason(),
                lead.getSaleValue(),
                overdueToAssign,
                overdueToFirstContact
        );
    }
}
