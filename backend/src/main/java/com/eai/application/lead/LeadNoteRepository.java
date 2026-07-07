package com.eai.application.lead;

import com.eai.domain.lead.LeadNote;

import java.util.List;
import java.util.UUID;

public interface LeadNoteRepository {

    List<LeadNote> findByLeadId(UUID leadId);

    LeadNote save(LeadNote note);
}
