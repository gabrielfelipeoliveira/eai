package com.eai.api.distribution;

import com.eai.application.distribution.LeadDashboardMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeadDashboardResponseTest {

    @DisplayName("Converte metricas do dashboard de distribuicao para resposta")
    @Test
    void mapsMetricsToResponse() {
        UUID sellerId = UUID.randomUUID();
        LeadDashboardResponse response = LeadDashboardResponse.fromMetrics(new LeadDashboardMetrics(
                3,
                2,
                List.of(new LeadDashboardMetrics.LeadsBySeller(sellerId, "Vendedor", 7))
        ));

        assertThat(response.unassignedLeads()).isEqualTo(3);
        assertThat(response.overdueLeads()).isEqualTo(2);
        assertThat(response.leadsBySeller().getFirst().sellerId()).isEqualTo(sellerId);
        assertThat(response.leadsBySeller().getFirst().leadCount()).isEqualTo(7);
    }
}
