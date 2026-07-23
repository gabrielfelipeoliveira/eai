package com.eai.application.dashboard;

import com.eai.application.common.ForbiddenException;
import com.eai.application.distribution.LeadSlaPolicyRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardAnalyticsServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final Instant CREATED_AT = Instant.parse("2026-01-10T10:00:00Z");

    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final LeadSlaPolicyRepository slaPolicyRepository = mock(LeadSlaPolicyRepository.class);
    private final DashboardAnalyticsService service = new DashboardAnalyticsService(leadRepository, userRepository, slaPolicyRepository);

    @DisplayName("Resumo agrega leads vendidos perdidos atribuidos e tempo medio de resposta")
    @Test
    void summaryAggregatesLeadMetrics() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.MANUAL, SELLER_ID, 20, BigDecimal.valueOf(50000)),
                lead(LeadStatus.LOST, LeadSource.WEBSITE, SELLER_ID, 40, null),
                lead(LeadStatus.AVAILABLE, LeadSource.FACEBOOK, null, null, null)
        ));
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID)).thenReturn(Optional.empty());

        DashboardSummary summary = service.summary(filters(), admin());

        assertThat(summary.totalLeadsToday()).isEqualTo(3);
        assertThat(summary.totalLeadsThisMonth()).isEqualTo(3);
        assertThat(summary.availableLeads()).isEqualTo(1);
        assertThat(summary.assignedLeads()).isEqualTo(2);
        assertThat(summary.soldLeads()).isEqualTo(1);
        assertThat(summary.lostLeads()).isEqualTo(1);
        assertThat(summary.conversionRate()).isEqualTo(50.0);
        assertThat(summary.averageFirstResponseTimeMinutes()).isEqualTo(30.0);
        assertThat(summary.overdueLeads()).isZero();
    }

    @DisplayName("Graficos agrupam leads por origem e status em ordem alfabetica do label")
    @Test
    void chartsGroupLeadsBySourceAndStatus() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.WEBSITE, SELLER_ID, 20, BigDecimal.valueOf(50000)),
                lead(LeadStatus.SOLD, LeadSource.WEBSITE, SELLER_ID, 30, BigDecimal.valueOf(30000)),
                lead(LeadStatus.AVAILABLE, LeadSource.FACEBOOK, null, null, null)
        ));

        assertThat(service.leadsBySource(filters(), admin()))
                .extracting(DashboardChartItem::label, DashboardChartItem::value)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple("FACEBOOK", 1L),
                        org.assertj.core.api.Assertions.tuple("WEBSITE", 2L)
                );

        assertThat(service.leadsByStatus(filters(), admin()))
                .extracting(DashboardChartItem::label, DashboardChartItem::value)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple("AVAILABLE", 1L),
                        org.assertj.core.api.Assertions.tuple("SOLD", 2L)
                );
    }

    @DisplayName("Ranking de vendedores usa nome cadastrado e calcula conversao")
    @Test
    void leadsBySellerUsesRegisteredNameAndConversionRate() {
        when(leadRepository.findAll(any())).thenReturn(List.of(
                lead(LeadStatus.SOLD, LeadSource.MANUAL, SELLER_ID, 20, BigDecimal.valueOf(50000)),
                lead(LeadStatus.LOST, LeadSource.MANUAL, SELLER_ID, 40, null),
                lead(LeadStatus.AVAILABLE, LeadSource.MANUAL, null, null, null)
        ));
        when(userRepository.findAll()).thenReturn(List.of(seller()));

        assertThat(service.leadsBySeller(filters(), admin()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.sellerId()).isEqualTo(SELLER_ID);
                    assertThat(item.sellerName()).isEqualTo("Ana Vendedora");
                    assertThat(item.leadCount()).isEqualTo(2);
                    assertThat(item.soldLeads()).isEqualTo(1);
                    assertThat(item.conversionRate()).isEqualTo(50.0);
                });
    }

    @DisplayName("Usuario sem papel permitido nao acessa dashboard")
    @Test
    void unsupportedRoleCannotAccessDashboard() {
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                "avaliador@eai.com",
                COMPANY_ID,
                STORE_ID,
                Set.of(UserRole.AVALIADOR)
        );

        assertThatThrownBy(() -> service.leadsByStatus(filters(), user))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Access denied for dashboard");
    }

    private DashboardFilters filters() {
        return new DashboardFilters(COMPANY_ID, STORE_ID, CREATED_AT.minusSeconds(3600), CREATED_AT.plusSeconds(3600));
    }

    private AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), "admin@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));
    }

    private User seller() {
        return new User(
                SELLER_ID,
                "Ana Vendedora",
                "ana@eai.com",
                "hash",
                null,
                null,
                COMPANY_ID,
                STORE_ID,
                UserStatus.ACTIVE,
                Set.of(UserRole.SELLER),
                CREATED_AT,
                CREATED_AT
        );
    }

    private Lead lead(LeadStatus status, LeadSource source, UUID sellerId, Integer firstResponseMinutes, BigDecimal saleValue) {
        Instant assignedAt = sellerId == null ? null : CREATED_AT;
        Instant firstContactAt = firstResponseMinutes == null ? null : CREATED_AT.plusSeconds(firstResponseMinutes * 60L);
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                null,
                "Sao Paulo",
                "Honda Civic",
                source,
                "Origem",
                status,
                sellerId,
                assignedAt,
                CREATED_AT,
                CREATED_AT,
                firstContactAt,
                firstContactAt,
                status == LeadStatus.LOST ? "Sem retorno" : null,
                saleValue
        );
    }
}
