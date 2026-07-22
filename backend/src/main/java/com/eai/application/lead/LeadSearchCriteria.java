package com.eai.application.lead;

import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;

import java.time.Instant;
import java.util.UUID;

public record LeadSearchCriteria(
        LeadStatus status,
        LeadSource source,
        UUID assignedToUserId,
        UUID storeId,
        Instant createdFrom,
        Instant createdTo,
        String text,
        String vehicle,
        String phone,
        UUID scopeCompanyId,
        UUID scopeStoreId,
        UUID visibleToSellerUserId
) {
    public LeadSearchCriteria(
            LeadStatus status,
            LeadSource source,
            UUID assignedToUserId,
            UUID storeId,
            Instant createdFrom,
            Instant createdTo,
            String text,
            String vehicle,
            String phone,
            UUID scopeCompanyId,
            UUID scopeStoreId
    ) {
        this(status, source, assignedToUserId, storeId, createdFrom, createdTo, text, vehicle, phone, scopeCompanyId, scopeStoreId, null);
    }
}
