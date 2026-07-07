package com.eai.api.lead;

import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;

import java.math.BigDecimal;
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
        Instant createdAt,
        Instant updatedAt,
        Instant firstContactAt,
        Instant lastContactAt,
        String lostReason,
        BigDecimal saleValue
) {

    public static LeadResponse fromDomain(Lead lead) {
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
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getFirstContactAt(),
                lead.getLastContactAt(),
                lead.getLostReason(),
                lead.getSaleValue()
        );
    }
}
