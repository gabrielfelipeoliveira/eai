package com.eai.application.distribution;

import com.eai.application.lead.LeadRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeadAssignmentStrategyTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @DisplayName("Round robin seleciona proximo vendedor apos atribuicao mais recente")

    @Test
    void roundRobinSelectsNextSellerAfterMostRecentAssignment() {
        LeadRepository repository = mock(LeadRepository.class);
        User ana = seller("Ana", "00000000-0000-0000-0000-000000000301");
        User bia = seller("Bia", "00000000-0000-0000-0000-000000000302");
        User caio = seller("Caio", "00000000-0000-0000-0000-000000000303");
        when(repository.findMostRecentAssignedSellerId(STORE_ID)).thenReturn(java.util.Optional.of(bia.getId()));

        RoundRobinAssignmentStrategy strategy = new RoundRobinAssignmentStrategy(repository);

        assertThat(strategy.selectSeller(lead(null, null), List.of(caio, bia, ana))).contains(caio);
    }

    @DisplayName("Menor carga seleciona vendedor com menos leads abertos")

    @Test
    void leastBusySelectsSellerWithFewerOpenLeads() {
        LeadRepository repository = mock(LeadRepository.class);
        User ana = seller("Ana", "00000000-0000-0000-0000-000000000301");
        User bia = seller("Bia", "00000000-0000-0000-0000-000000000302");
        when(repository.countOpenByAssignedToUserId(ana.getId())).thenReturn(3L);
        when(repository.countOpenByAssignedToUserId(bia.getId())).thenReturn(1L);

        LeastBusySellerAssignmentStrategy strategy = new LeastBusySellerAssignmentStrategy(repository);

        assertThat(strategy.selectSeller(lead(null, null), List.of(ana, bia))).contains(bia);
    }

    private Lead lead(UUID assignedToUserId, Instant assignedAt) {
        Instant now = Instant.parse("2026-07-07T12:00:00Z");
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                null,
                null,
                "Civic",
                LeadSource.MANUAL,
                null,
                assignedToUserId == null ? LeadStatus.AVAILABLE : LeadStatus.ASSIGNED,
                assignedToUserId,
                assignedAt,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }

    private User seller(String name, String id) {
        Instant now = Instant.parse("2026-07-07T12:00:00Z");
        return new User(
                UUID.fromString(id),
                name,
                name.toLowerCase() + "@eai.com",
                "hash",
                null,
                null,
                COMPANY_ID,
                STORE_ID,
                UserStatus.ACTIVE,
                Set.of(UserRole.SELLER),
                now,
                now
        );
    }
}
