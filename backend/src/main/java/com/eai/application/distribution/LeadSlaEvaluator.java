package com.eai.application.distribution;

import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;

import java.time.Duration;
import java.time.Instant;

public class LeadSlaEvaluator {

    public boolean isOverdueToAssign(Lead lead, LeadSlaPolicy policy, Instant now) {
        return policy.isActive()
                && lead.getAssignedToUserId() == null
                && Duration.between(lead.getCreatedAt(), now).toMinutes() > policy.getMinutesToAssign();
    }

    public boolean isOverdueToFirstContact(Lead lead, LeadSlaPolicy policy, Instant now) {
        Instant start = lead.getAssignedAt() == null ? lead.getCreatedAt() : lead.getAssignedAt();
        return policy.isActive()
                && lead.getAssignedToUserId() != null
                && lead.getFirstContactAt() == null
                && Duration.between(start, now).toMinutes() > policy.getMinutesToFirstContact();
    }
}
