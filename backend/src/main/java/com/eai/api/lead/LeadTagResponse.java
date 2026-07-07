package com.eai.api.lead;

import com.eai.domain.lead.LeadTag;

import java.util.UUID;

public record LeadTagResponse(UUID id, UUID leadId, String name) {

    public static LeadTagResponse fromDomain(LeadTag tag) {
        return new LeadTagResponse(tag.getId(), tag.getLeadId(), tag.getName());
    }
}
