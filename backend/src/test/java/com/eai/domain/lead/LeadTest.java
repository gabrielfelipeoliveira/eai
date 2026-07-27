package com.eai.domain.lead;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID RELATED_LEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    @DisplayName("Criacao normaliza campos opcionais telefones adicionais e moeda")
    @Test
    void createNormalizesOptionalFieldsAdditionalPhonesAndCurrency() {
        Lead lead = Lead.create(
                COMPANY_ID,
                STORE_ID,
                " Cliente Teste ",
                "11999990000",
                List.of("11999990000", " 11888880000 ", "", "11888880000", "11777770000"),
                " cliente@eai.com ",
                " Sao Paulo ",
                " Civic ",
                null,
                null,
                LeadSource.MANUAL,
                " Mensagem ",
                null,
                " Sem retorno ",
                BigDecimal.valueOf(125000),
                " usd "
        );

        assertThat(lead.getCustomerName()).isEqualTo("Cliente Teste");
        assertThat(lead.getCustomerPhone()).isEqualTo("11999990000");
        assertThat(lead.getAdditionalPhones()).containsExactly("11888880000", "11777770000");
        assertThat(lead.getCustomerEmail()).isEqualTo("cliente@eai.com");
        assertThat(lead.getCustomerCity()).isEqualTo("Sao Paulo");
        assertThat(lead.getVehicleInterest()).isEqualTo("Civic");
        assertThat(lead.getOriginalMessage()).isEqualTo("Mensagem");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.AVAILABLE);
        assertThat(lead.getLostReason()).isEqualTo("Sem retorno");
        assertThat(lead.getSaleCurrency()).isEqualTo("USD");
    }

    @DisplayName("Lead de origem automatica com vendedor nasce atribuido")
    @Test
    void automatedLeadWithSellerStartsAssigned() {
        Lead lead = Lead.create(
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                null,
                null,
                "Civic",
                LeadSource.WEBSITE,
                null,
                SELLER_ID,
                null,
                null
        );

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(lead.getAssignedToUserId()).isEqualTo(SELLER_ID);
        assertThat(lead.getAssignedAt()).isNotNull();
        assertThat(lead.getSaleCurrency()).isEqualTo("BRL");
    }

    @DisplayName("Atualizacao remove responsavel e normaliza campos financeiros")
    @Test
    void updateClearsAssignmentAndNormalizesCurrency() {
        Lead lead = lead(LeadStatus.ASSIGNED, SELLER_ID);

        lead.update(
                COMPANY_ID,
                STORE_ID,
                "Cliente Atualizado",
                null,
                List.of("11999990000"),
                null,
                null,
                null,
                null,
                null,
                LeadSource.FACEBOOK,
                " ",
                LeadStatus.AVAILABLE,
                null,
                NOW,
                null,
                null,
                " ",
                BigDecimal.TEN,
                "eur"
        );

        assertThat(lead.getCustomerPhone()).isNull();
        assertThat(lead.getAdditionalPhones()).containsExactly("11999990000");
        assertThat(lead.getAssignedToUserId()).isNull();
        assertThat(lead.getAssignedAt()).isNull();
        assertThat(lead.getOriginalMessage()).isNull();
        assertThat(lead.getLostReason()).isNull();
        assertThat(lead.getSaleCurrency()).isEqualTo("EUR");
    }

    @DisplayName("Mudanca para primeiro contato preenche primeiro e ultimo contato")
    @Test
    void changeStatusToFirstContactSetsContactTimestamps() {
        Lead lead = lead(LeadStatus.ASSIGNED, SELLER_ID);

        LeadStatus previousStatus = lead.changeStatus(LeadStatus.FIRST_CONTACT);

        assertThat(previousStatus).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.FIRST_CONTACT);
        assertThat(lead.getFirstContactAt()).isNotNull();
        assertThat(lead.getLastContactAt()).isNotNull();
    }

    @DisplayName("Mudancas comerciais atualizam ultimo contato sem sobrescrever primeiro contato existente")
    @Test
    void commercialStatusChangesSetLastContactOnly() {
        Lead lead = lead(LeadStatus.FIRST_CONTACT, SELLER_ID);
        Instant firstContactAt = lead.getFirstContactAt();

        lead.changeStatus(LeadStatus.IN_NEGOTIATION);
        lead.changeStatus(LeadStatus.VISIT_SCHEDULED);
        lead.changeStatus(LeadStatus.SIMULATING);
        lead.changeStatus(LeadStatus.PROPOSAL_SENT);
        lead.changeStatus(LeadStatus.PROPOSAL_APPROVED);
        lead.changeStatus(LeadStatus.SOLD);
        lead.changeStatus(LeadStatus.LOST);

        assertThat(lead.getFirstContactAt()).isEqualTo(firstContactAt);
        assertThat(lead.getLastContactAt()).isNotNull();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.LOST);
    }

    @DisplayName("Atribuicao define vendedor data e status")
    @Test
    void assignToSetsSellerDateAndStatus() {
        Lead lead = lead(LeadStatus.AVAILABLE, null);

        LeadStatus previousStatus = lead.assignTo(SELLER_ID);

        assertThat(previousStatus).isEqualTo(LeadStatus.AVAILABLE);
        assertThat(lead.getAssignedToUserId()).isEqualTo(SELLER_ID);
        assertThat(lead.getAssignedAt()).isNotNull();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.ASSIGNED);
    }

    @DisplayName("Duplicidade referencia lead relacionado e bloqueia autorreferencia")
    @Test
    void markDuplicatedStoresRelatedLeadAndRejectsSelfReference() {
        Lead lead = lead(LeadStatus.NEW, null);

        LeadStatus previousStatus = lead.markDuplicated(RELATED_LEAD_ID);

        assertThat(previousStatus).isEqualTo(LeadStatus.NEW);
        assertThat(lead.getRelatedLeadId()).isEqualTo(RELATED_LEAD_ID);
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.DUPLICATED);
        assertThatThrownBy(() -> lead.markDuplicated(lead.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relatedLeadId");
    }

    @DisplayName("Lista de telefones adicionais exposta e imutavel")
    @Test
    void additionalPhonesAreImmutable() {
        Lead lead = Lead.create(
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                List.of("11888880000"),
                null,
                null,
                null,
                null,
                null,
                LeadSource.MANUAL,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> lead.getAdditionalPhones().add("11777770000"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @DisplayName("Moeda invalida e nome vazio sao rejeitados")
    @Test
    void invalidRequiredFieldsAreRejected() {
        assertThatThrownBy(() -> leadWithName(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerName");

        assertThatThrownBy(() -> new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                LeadSource.MANUAL,
                null,
                LeadStatus.NEW,
                null,
                null,
                NOW,
                NOW,
                null,
                null,
                null,
                null,
                "REAL",
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("saleCurrency");
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId) {
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                "cliente@eai.com",
                "Sao Paulo",
                "Civic",
                LeadSource.MANUAL,
                "Origem",
                status,
                assignedToUserId,
                assignedToUserId == null ? null : NOW,
                NOW,
                NOW,
                status == LeadStatus.FIRST_CONTACT ? NOW : null,
                status == LeadStatus.FIRST_CONTACT ? NOW : null,
                null,
                null
        );
    }

    private Lead leadWithName(String name) {
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                name,
                "11999990000",
                null,
                null,
                null,
                LeadSource.MANUAL,
                null,
                LeadStatus.NEW,
                null,
                null,
                NOW,
                NOW,
                null,
                null,
                null,
                null
        );
    }
}
