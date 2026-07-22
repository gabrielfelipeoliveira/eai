package com.eai.infrastructure.persistence.email;

import com.eai.application.email.EmailImportHistoryRepository;
import com.eai.domain.email.EmailImportHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailImportHistoryPersistenceAdapter implements EmailImportHistoryRepository {

    private final SpringDataEmailImportHistoryRepository repository;

    @Override
    public EmailImportHistory save(EmailImportHistory history) {
        return toDomain(repository.save(toEntity(history)));
    }

    private EmailImportHistory toDomain(EmailImportHistoryJpaEntity entity) {
        return new EmailImportHistory(
                entity.getId(),
                entity.getEmailAccountId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getStatus(),
                entity.getMessagesRead(),
                entity.getLeadsCreated(),
                entity.getDuplicatesMarked(),
                entity.getMessage(),
                entity.getStartedAt(),
                entity.getFinishedAt()
        );
    }

    private EmailImportHistoryJpaEntity toEntity(EmailImportHistory history) {
        EmailImportHistoryJpaEntity entity = new EmailImportHistoryJpaEntity();
        entity.setId(history.getId());
        entity.setEmailAccountId(history.getEmailAccountId());
        entity.setCompanyId(history.getCompanyId());
        entity.setStoreId(history.getStoreId());
        entity.setStatus(history.getStatus());
        entity.setMessagesRead(history.getMessagesRead());
        entity.setLeadsCreated(history.getLeadsCreated());
        entity.setDuplicatesMarked(history.getDuplicatesMarked());
        entity.setMessage(history.getMessage());
        entity.setStartedAt(history.getStartedAt());
        entity.setFinishedAt(history.getFinishedAt());
        return entity;
    }
}
