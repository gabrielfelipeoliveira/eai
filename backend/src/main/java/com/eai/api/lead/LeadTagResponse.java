package com.eai.api.lead;

import com.eai.domain.lead.LeadTag;

import java.util.UUID;

public record LeadTagResponse(UUID id, UUID leadId, UUID tagId, String name, String type) {

    public static LeadTagResponse fromDomain(LeadTag tag) {
        return new LeadTagResponse(tag.getId(), tag.getLeadId(), tag.getTagId(), tag.getName(), tag.getType());
    }
}
