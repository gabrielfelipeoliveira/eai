package com.eai.api.lead;

import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadStatus;

import java.time.Instant;
import java.util.UUID;

public record LeadHistoryResponse(
        UUID id,
        UUID leadId,
        UUID userId,
        LeadStatus previousStatus,
        LeadStatus newStatus,
        String description,
        Instant createdAt
) {

    public static LeadHistoryResponse fromDomain(LeadHistory history) {
        return new LeadHistoryResponse(
                history.getId(),
                history.getLeadId(),
                history.getUserId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
