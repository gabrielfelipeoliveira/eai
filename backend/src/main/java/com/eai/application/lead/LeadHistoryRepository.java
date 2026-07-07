package com.eai.application.lead;

import com.eai.domain.lead.LeadHistory;

import java.util.List;
import java.util.UUID;

public interface LeadHistoryRepository {

    List<LeadHistory> findByLeadId(UUID leadId);

    LeadHistory save(LeadHistory history);
}
