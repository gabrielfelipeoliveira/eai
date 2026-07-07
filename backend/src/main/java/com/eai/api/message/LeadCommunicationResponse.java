package com.eai.api.message;

import com.eai.domain.message.LeadCommunication;
import com.eai.domain.message.LeadCommunicationChannel;

import java.time.Instant;
import java.util.UUID;

public record LeadCommunicationResponse(
        UUID id,
        UUID leadId,
        UUID userId,
        LeadCommunicationChannel channel,
        UUID templateId,
        String message,
        Instant createdAt
) {
    public static LeadCommunicationResponse fromDomain(LeadCommunication communication) {
        return new LeadCommunicationResponse(
                communication.getId(),
                communication.getLeadId(),
                communication.getUserId(),
                communication.getChannel(),
                communication.getTemplateId(),
                communication.getMessage(),
                communication.getCreatedAt()
        );
    }
}
