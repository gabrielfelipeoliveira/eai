package com.eai.application.report;

import com.eai.application.common.ForbiddenException;
import com.eai.application.distribution.LeadSlaPolicyRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.LeadSearchCriteria;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID OTHER_SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");
    private static final Instant DAY_ONE = Instant.parse("2026-07-20T10:00:00Z");
    private static final Instant DAY_TWO = Instant.parse("2026-07-21T10:00:00Z");

    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final LeadSlaPolicyRepository slaPolicyRepository = mock(LeadSlaPolicyRepository.class);
    private final ReportExporter exporter = mock(ReportExporter.class);
    private final ReportService service = new ReportService(leadRepository, userRepository, slaPolicyRepository, List.of(exporter));

    @DisplayName("Agrupa leads por periodo com vendidos perdidos e taxa de conversao")
    @Test
    void leadsByPeriodGroupsClosedLeadsAndConversionRate() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(3600), BigDecimal.valueOf(100000), null),
                lead(LeadStatus.LOST, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(7200), null, "Sem retorno"),
                lead(LeadStatus.AVAILABLE, LeadSource.FACEBOOK, null, DAY_TWO, null, null, null)
        ));

        List<ReportLeadPeriodItem> items = service.leadsByPeriod(filters(), admin());

        assertThat(items)
                .extracting(ReportLeadPeriodItem::period, ReportLeadPeriodItem::leadCount, ReportLeadPeriodItem::soldLeads, ReportLeadPeriodItem::lostLeads, ReportLeadPeriodItem::conversionRate)
                .containsExactly(
                        tuple("2026-07-20", 2L, 1L, 1L, 50.0),
                        tuple("2026-07-21", 1L, 0L, 0L, 0.0)
                );
    }

    @DisplayName("Agrupa leads por origem em ordem alfabetica")
    @Test
    void leadsBySourceGroupsAndSortsBySource() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE, BigDecimal.TEN, null),
                lead(LeadStatus.LOST, LeadSource.FACEBOOK, SELLER_ID, DAY_ONE, DAY_ONE, null, "Preco"),
                lead(LeadStatus.LOST, LeadSource.WEBSITE, OTHER_SELLER_ID, DAY_ONE, DAY_ONE, null, "Sem retorno")
        ));

        List<ReportSourceItem> items = service.leadsBySource(filters(), admin());

        assertThat(items)
                .extracting(ReportSourceItem::source, ReportSourceItem::leadCount, ReportSourceItem::soldLeads, ReportSourceItem::lostLeads, ReportSourceItem::conversionRate)
                .containsExactly(
                        tuple("FACEBOOK", 1L, 0L, 1L, 0.0),
                        tuple("WEBSITE", 2L, 1L, 1L, 50.0)
                );
    }

    @DisplayName("Ranking de vendedores calcula conversao tempo medio e valor vendido")
    @Test
    void leadsBySellerCalculatesSellerMetrics() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(3600), BigDecimal.valueOf(100000), null),
                lead(LeadStatus.LOST, LeadSource.MANUAL, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(1800), null, "Sem retorno"),
                lead(LeadStatus.SOLD, LeadSource.MANUAL, OTHER_SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(600), BigDecimal.valueOf(50000), null),
                lead(LeadStatus.AVAILABLE, LeadSource.FACEBOOK, null, DAY_ONE, null, null, null)
        ));
        when(userRepository.findAll()).thenReturn(List.of(seller(SELLER_ID, "Ana Seller")));

        List<ReportSellerItem> items = service.leadsBySeller(filters(), admin());

        assertThat(items).hasSize(2);
        assertThat(items.get(0).sellerName()).isEqualTo("Ana Seller");
        assertThat(items.get(0).leadCount()).isEqualTo(2);
        assertThat(items.get(0).soldLeads()).isEqualTo(1);
        assertThat(items.get(0).lostLeads()).isEqualTo(1);
        assertThat(items.get(0).conversionRate()).isEqualTo(50.0);
        assertThat(items.get(0).averageFirstResponseTimeMinutes()).isEqualTo(45.0);
        assertThat(items.get(0).saleValue()).isEqualByComparingTo("100000");
        assertThat(items.get(1).sellerId()).isEqualTo(OTHER_SELLER_ID);
        assertThat(items.get(1).sellerName()).isEqualTo("Vendedor sem cadastro");
        assertThat(items.get(1).soldLeads()).isEqualTo(1);
        assertThat(items.get(1).saleValue()).isEqualByComparingTo("50000");
    }

    @DisplayName("Lista perdidos e vendas usando data de fechamento e nome do vendedor")
    @Test
    void lostLeadsAndSalesUseClosedAtAndSellerName() {
        Lead lost = lead(LeadStatus.LOST, LeadSource.FACEBOOK, SELLER_ID, DAY_ONE, DAY_TWO, null, "Sem retorno");
        Lead sold = lead(LeadStatus.SOLD, LeadSource.WEBSITE, OTHER_SELLER_ID, DAY_TWO, null, BigDecimal.valueOf(90000), null);
        when(leadRepository.findAll(any())).thenReturn(List.of(lost, sold));
        when(userRepository.findAll()).thenReturn(List.of(seller(SELLER_ID, "Ana Seller")));

        List<ReportLostLeadItem> lostLeads = service.lostLeads(filters(), admin());
        List<ReportSaleItem> sales = service.sales(filters(), admin());

        assertThat(lostLeads).singleElement().satisfies(item -> {
            assertThat(item.leadId()).isEqualTo(lost.getId());
            assertThat(item.sellerName()).isEqualTo("Ana Seller");
            assertThat(item.lostReason()).isEqualTo("Sem retorno");
            assertThat(item.lostAt()).isEqualTo(DAY_TWO);
        });
        assertThat(sales).singleElement().satisfies(item -> {
            assertThat(item.leadId()).isEqualTo(sold.getId());
            assertThat(item.sellerName()).isEqualTo("Vendedor sem cadastro");
            assertThat(item.saleValue()).isEqualByComparingTo("90000");
            assertThat(item.soldAt()).isEqualTo(sold.getUpdatedAt());
        });
    }

    @DisplayName("Relatorio de SLA conta atrasos e contatos dentro e fora do prazo")
    @Test
    void slaReportCalculatesOverdueAndFirstContactBuckets() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.AVAILABLE, LeadSource.MANUAL, null, DAY_ONE.minusSeconds(3600), null, null, null),
                lead(LeadStatus.ASSIGNED, LeadSource.WEBSITE, SELLER_ID, DAY_ONE.minusSeconds(3600), null, null, null),
                lead(LeadStatus.FIRST_CONTACT, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(600), null, null),
                lead(LeadStatus.FIRST_CONTACT, LeadSource.WEBSITE, SELLER_ID, DAY_ONE, DAY_ONE.plusSeconds(3600), null, null)
        ));
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true)));

        ReportSlaSummary summary = service.sla(filters(), admin());

        assertThat(summary.leadCount()).isEqualTo(4);
        assertThat(summary.overdueToAssign()).isEqualTo(1);
        assertThat(summary.overdueToFirstContact()).isEqualTo(1);
        assertThat(summary.overdueTotal()).isEqualTo(2);
        assertThat(summary.averageFirstResponseTimeMinutes()).isEqualTo(35.0);
        assertThat(summary.firstContactWithinSla()).isEqualTo(1);
        assertThat(summary.firstContactOutsideSla()).isEqualTo(1);
    }

    @DisplayName("Exportacao CSV delega para exporter configurado")
    @Test
    void csvExportsDelegateToConfiguredExporter() {
        ExportedReport leadPeriods = new ExportedReport("leads.csv", "text/csv", "leads".getBytes());
        ExportedReport sellers = new ExportedReport("sellers.csv", "text/csv", "sellers".getBytes());
        when(exporter.supports(ReportExportFormat.CSV)).thenReturn(true);
        when(exporter.exportLeadPeriods(any())).thenReturn(leadPeriods);
        when(exporter.exportSellers(any())).thenReturn(sellers);
        when(leadRepository.findAll(any())).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(service.exportLeadPeriodsCsv(filters(), admin())).isSameAs(leadPeriods);
        assertThat(service.exportSellersCsv(filters(), admin())).isSameAs(sellers);
    }

    @DisplayName("Falha quando nenhum exporter suporta CSV")
    @Test
    void exportFailsWhenNoExporterSupportsCsv() {
        when(exporter.supports(ReportExportFormat.CSV)).thenReturn(false);

        assertThatThrownBy(() -> service.exportLeadPeriodsCsv(filters(), admin()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Report exporter not configured");
    }

    @DisplayName("Escopo do vendedor restringe loja e vendedor autenticado")
    @Test
    void sellerScopeRestrictsStoreAndSeller() {
        when(leadRepository.findAll(any())).thenReturn(List.of());
        ArgumentCaptor<LeadSearchCriteria> captor = ArgumentCaptor.forClass(LeadSearchCriteria.class);

        service.leadsByPeriod(filters(), sellerUser());

        verify(leadRepository).findAll(captor.capture());
        assertThat(captor.getValue().scopeCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(captor.getValue().scopeStoreId()).isEqualTo(STORE_ID);
        assertThat(captor.getValue().assignedToUserId()).isEqualTo(SELLER_ID);
    }

    @DisplayName("Usuario sem papel permitido nao acessa relatorios")
    @Test
    void unsupportedRoleCannotAccessReports() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "avaliador@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.AVALIADOR));

        assertThatThrownBy(() -> service.leadsByPeriod(filters(), user))
                .isInstanceOf(ForbiddenException.class);
    }

    private ReportFilters filters() {
        return new ReportFilters(COMPANY_ID, STORE_ID, null, null, DAY_ONE.minusSeconds(3600), DAY_TWO.plusSeconds(3600));
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@eai.com", null, null, Set.of(UserRole.ADMIN));
    }

    private AuthenticatedUser sellerUser() {
        return new AuthenticatedUser(SELLER_ID, "seller@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.SELLER));
    }

    private Lead lead(
            LeadStatus status,
            LeadSource source,
            UUID sellerId,
            Instant createdAt,
            Instant firstContactAt,
            BigDecimal saleValue,
            String lostReason
    ) {
        Instant assignedAt = sellerId == null ? null : createdAt;
        Instant updatedAt = createdAt.plusSeconds(120);
        Instant lastContactAt = status == LeadStatus.SOLD || status == LeadStatus.LOST || status == LeadStatus.FIRST_CONTACT
                ? firstContactAt
                : null;
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                "cliente@eai.com",
                "Sao Paulo",
                "Honda Civic",
                source,
                "Origem",
                status,
                sellerId,
                assignedAt,
                createdAt,
                updatedAt,
                firstContactAt,
                lastContactAt,
                lostReason,
                saleValue
        );
    }

    private User seller(UUID id, String name) {
        return new User(id, name, "seller-" + id + "@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, UserStatus.ACTIVE, Set.of(UserRole.SELLER), DAY_ONE, DAY_ONE);
    }
}
