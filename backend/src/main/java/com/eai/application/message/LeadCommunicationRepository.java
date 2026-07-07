package com.eai.application.message;

import com.eai.domain.message.LeadCommunication;

import java.util.List;
import java.util.UUID;

public interface LeadCommunicationRepository {

    List<LeadCommunication> findByLeadId(UUID leadId);

    LeadCommunication save(LeadCommunication communication);
}
