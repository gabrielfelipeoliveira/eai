package com.eai.api.lead;

import com.eai.domain.lead.LeadNote;

import java.time.Instant;
import java.util.UUID;

public record LeadNoteResponse(UUID id, UUID leadId, UUID userId, String note, Instant createdAt, Instant updatedAt) {

    public static LeadNoteResponse fromDomain(LeadNote note) {
        return new LeadNoteResponse(note.getId(), note.getLeadId(), note.getUserId(), note.getNote(), note.getCreatedAt(), note.getUpdatedAt());
    }
}
