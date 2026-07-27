package com.eai.infrastructure.persistence.lead;

import com.eai.application.item.ItemRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.lead.PageResult;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadPersistenceAdapterTest {

    private static final UUID LEAD_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    private final SpringDataLeadRepository repository = mock(SpringDataLeadRepository.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final LeadPersistenceAdapter adapter = new LeadPersistenceAdapter(repository, itemRepository);

    @DisplayName("Busca paginada converte entidades em dominio e preserva metadados")
    @Test
    @SuppressWarnings("unchecked")
    void searchMapsPageResult() {
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity()), PageRequest.of(1, 5), 6));

        PageResult<Lead> result = adapter.search(criteria(), 1, 5);

        assertThat(result.content().getFirst().getId()).isEqualTo(LEAD_ID);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(6);
    }

    @DisplayName("Consultas especificas de lead delegam para o repositorio Spring Data")
    @Test
    @SuppressWarnings("unchecked")
    void delegatesSpecificQueries() {
        LeadJpaEntity entity = entity();
        when(repository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(entity));
        when(repository.findById(LEAD_ID)).thenReturn(Optional.of(entity));
        when(repository.findByStoreIdAndAssignedToUserIdIsNullAndStatusInOrderByCreatedAtAsc(eq(STORE_ID), any())).thenReturn(List.of(entity));
        when(repository.findByStoreIdAndStatusInOrderByCreatedAtAsc(eq(STORE_ID), any())).thenReturn(List.of(entity));
        when(repository.findTopByStoreIdAndAssignedToUserIdIsNotNullOrderByAssignedAtDescUpdatedAtDesc(STORE_ID)).thenReturn(Optional.of(entity));
        when(repository.findByStoreIdAndAnyPhoneOrderByCreatedAtDesc(eq(STORE_ID), eq(List.of("+5511999990000")))).thenReturn(List.of(entity));
        when(repository.countByAssignedToUserIdAndStatusIn(eq(SELLER_ID), any())).thenReturn(4L);

        assertThat(adapter.findAll(criteria())).hasSize(1);
        assertThat(adapter.findById(LEAD_ID)).isPresent();
        assertThat(adapter.findPendingByStoreId(STORE_ID)).hasSize(1);
        assertThat(adapter.findOverdueCandidatesByStoreId(STORE_ID)).hasSize(1);
        assertThat(adapter.findMostRecentAssignedSellerId(STORE_ID)).contains(SELLER_ID);
        assertThat(adapter.findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999990000"))).isPresent();
        assertThat(adapter.findMostRecentByStoreIdAndAnyPhone(STORE_ID, List.of())).isEmpty();
        assertThat(adapter.countOpenByAssignedToUserId(SELLER_ID)).isEqualTo(4);
        assertThat(adapter.existsByStoreIdAndAnyPhone(STORE_ID, List.of("+5511999990000"))).isTrue();
    }

    @DisplayName("Salva lead convertendo dominio para entidade JPA")
    @Test
    void savesLeadMappingDomainToEntity() {
        when(repository.save(any(LeadJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead saved = adapter.save(domain());

        assertThat(saved.getId()).isEqualTo(LEAD_ID);
        assertThat(saved.getAdditionalPhones()).containsExactly("+5511988880000");
        verify(repository).save(any(LeadJpaEntity.class));
    }

    private LeadSearchCriteria criteria() {
        return new LeadSearchCriteria(
                LeadStatus.ASSIGNED,
                LeadSource.MANUAL,
                SELLER_ID,
                STORE_ID,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-07-31T23:59:59Z"),
                "Joao 11999990000",
                "Civic",
                "+5511999990000",
                COMPANY_ID,
                STORE_ID,
                SELLER_ID
        );
    }

    private Lead domain() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Lead(
                LEAD_ID,
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "+5511999990000",
                List.of("+5511988880000"),
                "cliente@eai.com",
                "Sao Paulo",
                "Honda Civic",
                null,
                null,
                LeadSource.MANUAL,
                "Mensagem",
                LeadStatus.ASSIGNED,
                SELLER_ID,
                now,
                now,
                now,
                now,
                now,
                "Sem retorno",
                new BigDecimal("100000.00"),
                "BRL",
                UUID.randomUUID()
        );
    }

    private LeadJpaEntity entity() {
        Lead lead = domain();
        LeadJpaEntity entity = new LeadJpaEntity();
        entity.setId(lead.getId());
        entity.setCompanyId(lead.getCompanyId());
        entity.setStoreId(lead.getStoreId());
        entity.setCustomerName(lead.getCustomerName());
        entity.setCustomerPhone(lead.getCustomerPhone());
        entity.setAdditionalPhones(new HashSet<>(lead.getAdditionalPhones()));
        entity.setCustomerEmail(lead.getCustomerEmail());
        entity.setCustomerCity(lead.getCustomerCity());
        entity.setVehicleInterest(lead.getVehicleInterest());
        entity.setItemId(lead.getItemId());
        entity.setSource(lead.getSource());
        entity.setOriginalMessage(lead.getOriginalMessage());
        entity.setStatus(lead.getStatus());
        entity.setAssignedToUserId(lead.getAssignedToUserId());
        entity.setAssignedAt(lead.getAssignedAt());
        entity.setCreatedAt(lead.getCreatedAt());
        entity.setUpdatedAt(lead.getUpdatedAt());
        entity.setFirstContactAt(lead.getFirstContactAt());
        entity.setLastContactAt(lead.getLastContactAt());
        entity.setLostReason(lead.getLostReason());
        entity.setSaleValue(lead.getSaleValue());
        entity.setSaleCurrency(lead.getSaleCurrency());
        entity.setRelatedLeadId(lead.getRelatedLeadId());
        return entity;
    }
}
