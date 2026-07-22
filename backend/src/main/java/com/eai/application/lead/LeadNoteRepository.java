package com.eai.application.lead;

import com.eai.domain.lead.LeadNote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadNoteRepository {

    List<LeadNote> findByLeadId(UUID leadId);

    Optional<LeadNote> findById(UUID id);

    LeadNote save(LeadNote note);
}
