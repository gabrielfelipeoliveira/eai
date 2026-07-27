package com.eai.infrastructure.persistence.email;

import com.eai.domain.email.EmailAccount;
import com.eai.domain.email.EmailAccountStatus;
import com.eai.domain.email.EmailProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailAccountPersistenceAdapterTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();

    private final SpringDataEmailAccountRepository repository = mock(SpringDataEmailAccountRepository.class);
    private final EmailAccountPersistenceAdapter adapter = new EmailAccountPersistenceAdapter(repository);

    @DisplayName("Busca contas convertendo entidades JPA para dominio")
    @Test
    void findsAccountsMappingEntitiesToDomain() {
        EmailAccountJpaEntity entity = entity();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findByActiveTrue()).thenReturn(List.of(entity));
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(entity));
        when(repository.findByStoreIdIn(List.of(STORE_ID))).thenReturn(List.of(entity));
        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        assertThat(adapter.findAll().getFirst().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(adapter.findActive().getFirst().getLastSyncStatus()).isEqualTo(EmailAccountStatus.SUCCESS);
        assertThat(adapter.findByCompanyId(COMPANY_ID).getFirst().getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(adapter.findByStoreIdIn(List.of(STORE_ID)).getFirst().getStoreId()).isEqualTo(STORE_ID);
        assertThat(adapter.findById(ACCOUNT_ID)).isPresent();
    }

    @DisplayName("Lista por lojas retorna vazio sem chamar repositorio quando filtro esta vazio")
    @Test
    void findByStoreIdInReturnsEmptyForEmptyFilter() {
        assertThat(adapter.findByStoreIdIn(List.of())).isEmpty();
    }

    @DisplayName("Salva conta convertendo dominio para entidade JPA")
    @Test
    void savesAccountMappingDomainToEntity() {
        when(repository.save(any(EmailAccountJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailAccount saved = adapter.save(account());

        assertThat(saved.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getEncryptedPassword()).isEqualTo("encrypted");
        verify(repository).save(any(EmailAccountJpaEntity.class));
    }

    @DisplayName("Remove conta por id delegando ao repositorio")
    @Test
    void deletesAccountById() {
        adapter.deleteById(ACCOUNT_ID);

        verify(repository).deleteById(ACCOUNT_ID);
    }

    private EmailAccount account() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new EmailAccount(
                ACCOUNT_ID, COMPANY_ID, STORE_ID, "Principal", "imap.eai.com", 993,
                "contato@eai.com", "encrypted", EmailProtocol.IMAP, true, true,
                now, now, now, EmailAccountStatus.SUCCESS, "ok", now
        );
    }

    private EmailAccountJpaEntity entity() {
        EmailAccount account = account();
        EmailAccountJpaEntity entity = new EmailAccountJpaEntity();
        entity.setId(account.getId());
        entity.setCompanyId(account.getCompanyId());
        entity.setStoreId(account.getStoreId());
        entity.setName(account.getName());
        entity.setHost(account.getHost());
        entity.setPort(account.getPort());
        entity.setUsername(account.getUsername());
        entity.setEncryptedPassword(account.getEncryptedPassword());
        entity.setProtocol(account.getProtocol());
        entity.setUseSsl(account.isUseSsl());
        entity.setActive(account.isActive());
        entity.setLastReadAt(account.getLastReadAt());
        entity.setCreatedAt(account.getCreatedAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        entity.setLastSyncStatus(account.getLastSyncStatus());
        entity.setLastSyncMessage(account.getLastSyncMessage());
        entity.setLastSyncAt(account.getLastSyncAt());
        return entity;
    }
}
