package com.eai.infrastructure.persistence.message;

import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
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

class MessageTemplatePersistenceAdapterTest {

    private static final UUID TEMPLATE_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();

    private final SpringDataMessageTemplateRepository repository = mock(SpringDataMessageTemplateRepository.class);
    private final MessageTemplatePersistenceAdapter adapter = new MessageTemplatePersistenceAdapter(repository);

    @DisplayName("Consulta templates convertendo entidades nao excluidas para dominio")
    @Test
    void findsTemplatesMappingEntitiesToDomain() {
        MessageTemplateJpaEntity entity = entity(null);
        when(repository.findByDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of(entity));
        when(repository.findByCompanyIdAndDeletedAtIsNullOrderByNameAsc(COMPANY_ID)).thenReturn(List.of(entity));
        when(repository.findByStoreScopeOrderByNameAsc(COMPANY_ID, STORE_ID)).thenReturn(List.of(entity));
        when(repository.findByActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(MessageTemplateMetaStatus.APPROVED)).thenReturn(List.of(entity));
        when(repository.findByCompanyIdAndActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(COMPANY_ID, MessageTemplateMetaStatus.APPROVED)).thenReturn(List.of(entity));
        when(repository.findActiveByStoreScopeOrderByNameAsc(COMPANY_ID, STORE_ID, MessageTemplateMetaStatus.APPROVED)).thenReturn(List.of(entity));
        when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.of(entity));

        assertThat(adapter.findAll()).hasSize(1);
        assertThat(adapter.findByCompanyId(COMPANY_ID)).hasSize(1);
        assertThat(adapter.findByStoreScope(COMPANY_ID, STORE_ID)).hasSize(1);
        assertThat(adapter.findActive()).hasSize(1);
        assertThat(adapter.findActiveByCompanyId(COMPANY_ID)).hasSize(1);
        assertThat(adapter.findActiveByStoreScope(COMPANY_ID, STORE_ID)).hasSize(1);
        assertThat(adapter.findById(TEMPLATE_ID)).isPresent();
    }

    @DisplayName("Ignora template excluido na busca por id e salva exclusao logica")
    @Test
    void findByIdIgnoresDeletedAndSoftDeleteSavesEntity() {
        when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.of(entity(Instant.parse("2026-07-08T10:00:00Z"))));
        when(repository.save(any(MessageTemplateJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(adapter.findById(TEMPLATE_ID)).isEmpty();
        assertThat(adapter.save(template()).getId()).isEqualTo(TEMPLATE_ID);
        adapter.softDelete(template());
        verify(repository, org.mockito.Mockito.times(2)).save(any(MessageTemplateJpaEntity.class));
    }

    private MessageTemplate template() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new MessageTemplate(TEMPLATE_ID, COMPANY_ID, STORE_ID, "follow_up", MessageTemplateType.FOLLOW_UP, "Ola", "pt-BR", MessageTemplateMetaStatus.APPROVED, true, now, now, null);
    }

    private MessageTemplateJpaEntity entity(Instant deletedAt) {
        MessageTemplate template = template();
        MessageTemplateJpaEntity entity = new MessageTemplateJpaEntity();
        entity.setId(template.getId());
        entity.setCompanyId(template.getCompanyId());
        entity.setStoreId(template.getStoreId());
        entity.setName(template.getName());
        entity.setType(template.getType());
        entity.setContent(template.getContent());
        entity.setLanguageCode(template.getLanguageCode());
        entity.setMetaStatus(template.getMetaStatus());
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedAt(template.getUpdatedAt());
        entity.setDeletedAt(deletedAt);
        return entity;
    }
}
