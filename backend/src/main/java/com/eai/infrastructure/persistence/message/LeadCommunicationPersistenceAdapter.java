package com.eai.infrastructure.persistence.message;

import com.eai.application.message.LeadCommunicationRepository;
import com.eai.domain.message.LeadCommunication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadCommunicationPersistenceAdapter implements LeadCommunicationRepository {

    private final SpringDataLeadCommunicationRepository repository;

    @Override
    public List<LeadCommunication> findByLeadId(UUID leadId) {
        return repository.findByLeadIdOrderByCreatedAtDesc(leadId).stream().map(this::toDomain).toList();
    }

    @Override
    public LeadCommunication save(LeadCommunication communication) {
        return toDomain(repository.save(toEntity(communication)));
    }

    private LeadCommunication toDomain(LeadCommunicationJpaEntity entity) {
        return new LeadCommunication(
                entity.getId(),
                entity.getLeadId(),
                entity.getUserId(),
                entity.getChannel(),
                entity.getTemplateId(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }

    private LeadCommunicationJpaEntity toEntity(LeadCommunication communication) {
        LeadCommunicationJpaEntity entity = new LeadCommunicationJpaEntity();
        entity.setId(communication.getId());
        entity.setLeadId(communication.getLeadId());
        entity.setUserId(communication.getUserId());
        entity.setChannel(communication.getChannel());
        entity.setTemplateId(communication.getTemplateId());
        entity.setMessage(communication.getMessage());
        entity.setCreatedAt(communication.getCreatedAt());
        return entity;
    }
}
