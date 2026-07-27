package com.eai.infrastructure.persistence.tenant;

import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
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

class StorePersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();

    private final SpringDataStoreRepository repository = mock(SpringDataStoreRepository.class);
    private final StorePersistenceAdapter adapter = new StorePersistenceAdapter(repository);

    @DisplayName("Consulta lojas convertendo entidades JPA para dominio")
    @Test
    void findsStoresMappingEntitiesToDomain() {
        StoreJpaEntity entity = entity();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(entity));
        when(repository.findByIdIn(List.of(STORE_ID))).thenReturn(List.of(entity));
        when(repository.findById(STORE_ID)).thenReturn(Optional.of(entity));

        assertThat(adapter.findAll()).hasSize(1);
        assertThat(adapter.findByCompanyId(COMPANY_ID).getFirst().getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(adapter.findByIdIn(List.of(STORE_ID)).getFirst().getId()).isEqualTo(STORE_ID);
        assertThat(adapter.findById(STORE_ID)).isPresent();
    }

    @DisplayName("Verifica documentos e salva loja convertendo dominio para entidade")
    @Test
    void documentChecksAndSaveDelegateToRepository() {
        when(repository.existsByDocument("123")).thenReturn(true);
        when(repository.existsByDocumentAndIdNot("123", STORE_ID)).thenReturn(true);
        when(repository.save(any(StoreJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(adapter.existsByDocument("123")).isTrue();
        assertThat(adapter.existsByDocumentAndIdNot("123", STORE_ID)).isTrue();
        assertThat(adapter.save(store()).getName()).isEqualTo("Loja Centro");
        verify(repository).save(any(StoreJpaEntity.class));
    }

    private Store store() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja Centro", "123", "loja@eai.com", "11999990000", "Sao Paulo", "SP", "Rua 1", TenantStatus.ACTIVE, now, now);
    }

    private StoreJpaEntity entity() {
        Store store = store();
        StoreJpaEntity entity = new StoreJpaEntity();
        entity.setId(store.getId());
        entity.setCompanyId(store.getCompanyId());
        entity.setName(store.getName());
        entity.setDocument(store.getDocument());
        entity.setEmail(store.getEmail());
        entity.setPhone(store.getPhone());
        entity.setCity(store.getCity());
        entity.setState(store.getState());
        entity.setAddress(store.getAddress());
        entity.setStatus(store.getStatus());
        entity.setCreatedAt(store.getCreatedAt());
        entity.setUpdatedAt(store.getUpdatedAt());
        return entity;
    }
}
