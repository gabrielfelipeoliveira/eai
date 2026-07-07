package com.eai.application.lead;

import com.eai.domain.lead.LeadTag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadTagRepository {

    List<LeadTag> findByLeadId(UUID leadId);

    Optional<LeadTag> findById(UUID id);

    LeadTag save(LeadTag tag);

    void deleteById(UUID id);
}
