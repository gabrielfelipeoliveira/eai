package com.eai.infrastructure.persistence.email;

import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailImportHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailImportHistoryPersistenceAdapterTest {

    private static final UUID HISTORY_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();

    private final SpringDataEmailImportHistoryRepository repository = mock(SpringDataEmailImportHistoryRepository.class);
    private final EmailImportHistoryPersistenceAdapter adapter = new EmailImportHistoryPersistenceAdapter(repository);

    @DisplayName("Salva historico de importacao convertendo dominio e entidade")
    @Test
    void savesImportHistoryMappingDomainAndEntity() {
        when(repository.save(any(EmailImportHistoryJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailImportHistory saved = adapter.save(history());

        assertThat(saved.getId()).isEqualTo(HISTORY_ID);
        assertThat(saved.getMessagesRead()).isEqualTo(10);
        assertThat(saved.getStatus()).isEqualTo(EmailAccountStatus.SUCCESS);
        verify(repository).save(any(EmailImportHistoryJpaEntity.class));
    }

    private EmailImportHistory history() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new EmailImportHistory(
                HISTORY_ID,
                ACCOUNT_ID,
                COMPANY_ID,
                STORE_ID,
                EmailAccountStatus.SUCCESS,
                10,
                4,
                2,
                "ok",
                now,
                now
        );
    }
}
