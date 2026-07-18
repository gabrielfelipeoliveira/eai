package com.eai.application.lead;

import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateLeadCommand(
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
        Instant firstContactAt,
        Instant lastContactAt,
        String lostReason,
        BigDecimal saleValue,
        String saleCurrency,
        LeadItemCommand item
) {
}
