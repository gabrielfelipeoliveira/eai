package com.eai.application.lead;

import com.eai.domain.lead.LeadSource;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLeadCommand(
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
        BigDecimal saleValue,
        String saleCurrency,
        LeadItemCommand item
) {
}
