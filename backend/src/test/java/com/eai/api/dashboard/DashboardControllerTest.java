package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardAnalyticsService;
import com.eai.application.dashboard.DashboardChartItem;
import com.eai.application.dashboard.DashboardFilters;
import com.eai.application.dashboard.DashboardSalesPeriodItem;
import com.eai.application.dashboard.DashboardSellerItem;
import com.eai.application.dashboard.DashboardSummary;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(UUID.randomUUID(), "admin@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.ADMIN));

    private final DashboardAnalyticsService analyticsService = mock(DashboardAnalyticsService.class);
    private final DashboardController controller = new DashboardController(analyticsService);

    @DisplayName("Resumo converte periodo local para filtro UTC inclusivo do dia final")
    @Test
    void summaryMapsLocalDatesToUtcFilter() {
        when(analyticsService.summary(any(DashboardFilters.class), any(AuthenticatedUser.class)))
                .thenReturn(new DashboardSummary(1, 2, 3, 4, 5, 6, 7.5, 8.5, 9));

        DashboardSummaryResponse response = controller.summary(
                COMPANY_ID,
                STORE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                USER
        );

        ArgumentCaptor<DashboardFilters> captor = ArgumentCaptor.forClass(DashboardFilters.class);
        verify(analyticsService).summary(captor.capture(), any(AuthenticatedUser.class));
        assertThat(captor.getValue().dateFrom()).isEqualTo(Instant.parse("2026-07-01T00:00:00Z"));
        assertThat(captor.getValue().dateTo()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
        assertThat(response.totalLeadsThisMonth()).isEqualTo(2);
        assertThat(response.conversionRate()).isEqualTo(7.5);
    }

    @DisplayName("Graficos e rankings sao convertidos para respostas da API")
    @Test
    void mapsChartsAndRankingsToResponses() {
        UUID sellerId = UUID.randomUUID();
        when(analyticsService.leadsBySource(any(), any())).thenReturn(List.of(new DashboardChartItem("EMAIL", 10)));
        when(analyticsService.leadsByStatus(any(), any())).thenReturn(List.of(new DashboardChartItem("SOLD", 3)));
        when(analyticsService.leadsBySeller(any(), any())).thenReturn(List.of(new DashboardSellerItem(sellerId, "Vendedor", 12, 4, 33.33)));
        when(analyticsService.salesByPeriod(any(), any())).thenReturn(List.of(new DashboardSalesPeriodItem("2026-07", 4, new BigDecimal("95000.00"))));

        assertThat(controller.leadsBySource(null, null, null, null, USER).getFirst().label()).isEqualTo("EMAIL");
        assertThat(controller.leadsByStatus(null, null, null, null, USER).getFirst().value()).isEqualTo(3);
        assertThat(controller.leadsBySeller(null, null, null, null, USER).getFirst().sellerId()).isEqualTo(sellerId);
        assertThat(controller.salesByPeriod(null, null, null, null, USER).getFirst().saleValue()).isEqualByComparingTo("95000.00");
    }
}
