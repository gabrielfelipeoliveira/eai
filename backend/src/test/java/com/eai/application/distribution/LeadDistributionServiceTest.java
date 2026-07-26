package com.eai.application.distribution;

import com.eai.application.lead.LeadHistoryRepository;
import com.eai.application.lead.LeadRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.distribution.LeadDistributionConfig;
import com.eai.domain.distribution.LeadDistributionMode;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadDistributionServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");

    private final LeadDistributionConfigRepository configRepository = mock(LeadDistributionConfigRepository.class);
    private final LeadSlaPolicyRepository slaPolicyRepository = mock(LeadSlaPolicyRepository.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StoreService storeService = mock(StoreService.class);
    private final RoundRobinAssignmentStrategy roundRobinAssignmentStrategy = new RoundRobinAssignmentStrategy(leadRepository);
    private final LeastBusySellerAssignmentStrategy leastBusySellerAssignmentStrategy = new LeastBusySellerAssignmentStrategy(leadRepository);
    private final LeadDistributionService service = new LeadDistributionService(
            configRepository,
            slaPolicyRepository,
            leadRepository,
            historyRepository,
            userRepository,
            storeService,
            new ManualAssignmentStrategy(),
            roundRobinAssignmentStrategy,
            leastBusySellerAssignmentStrategy
    );

    @DisplayName("Atualiza configuracao de distribuicao e SLA para gerente da empresa")
    @Test
    void updatesSettingsForCompanyManager() {
        when(storeService.findRequired(STORE_ID)).thenReturn(store());
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID)).thenReturn(Optional.empty());
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID)).thenReturn(Optional.empty());
        when(configRepository.save(any(LeadDistributionConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(slaPolicyRepository.save(any(LeadSlaPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeadDistributionSettings settings = service.updateSettings(
                new UpdateLeadDistributionSettingsCommand(COMPANY_ID, STORE_ID, LeadDistributionMode.ROUND_ROBIN, true, 10, 20, true),
                manager()
        );

        assertThat(settings.distributionConfig().getMode()).isEqualTo(LeadDistributionMode.ROUND_ROBIN);
        assertThat(settings.distributionConfig().isActive()).isTrue();
        assertThat(settings.slaPolicy().getMinutesToAssign()).isEqualTo(10);
        assertThat(settings.slaPolicy().getMinutesToFirstContact()).isEqualTo(20);
        assertThat(settings.slaPolicy().isActive()).isTrue();
    }

    @DisplayName("Atribui lead automaticamente para vendedor ativo e registra historico")
    @Test
    void assignsAutomaticallyToActiveSellerAndRecordsHistory() {
        Lead lead = lead(LeadStatus.AVAILABLE, null, null, Instant.parse("2026-07-26T10:00:00Z"), null);
        User seller = seller(SELLER_ID, UserStatus.ACTIVE);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(storeService.findRequired(STORE_ID)).thenReturn(store());
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadDistributionConfig.create(COMPANY_ID, STORE_ID, LeadDistributionMode.ROUND_ROBIN, true)));
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(seller, seller(UUID.randomUUID(), UserStatus.INACTIVE)));
        when(leadRepository.findMostRecentAssignedSellerId(STORE_ID)).thenReturn(Optional.empty());
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead assignedLead = service.assignAutomatically(lead.getId(), manager());

        assertThat(assignedLead.getAssignedToUserId()).isEqualTo(SELLER_ID);
        assertThat(assignedLead.getStatus()).isEqualTo(LeadStatus.ASSIGNED);
        verify(historyRepository).save(argThat(history ->
                history.getLeadId().equals(lead.getId())
                        && history.getUserId().equals(MANAGER_ID)
                        && history.getPreviousStatus() == LeadStatus.AVAILABLE
                        && history.getNewStatus() == LeadStatus.ASSIGNED
        ));
    }

    @DisplayName("Lista apenas leads que violam SLA ativo")
    @Test
    void listsOnlyLeadsViolatingActiveSla() {
        Lead overdueLead = lead(LeadStatus.AVAILABLE, null, null, Instant.parse("2026-07-26T10:00:00Z"), null);
        Lead contactedLead = lead(LeadStatus.FIRST_CONTACT, SELLER_ID, Instant.parse("2026-07-26T10:05:00Z"), Instant.parse("2026-07-26T10:00:00Z"), Instant.parse("2026-07-26T10:10:00Z"));
        when(storeService.findRequired(STORE_ID)).thenReturn(store());
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true)));
        when(leadRepository.findOverdueCandidatesByStoreId(STORE_ID)).thenReturn(List.of(overdueLead, contactedLead));

        List<Lead> overdue = service.listOverdue(manager());

        assertThat(overdue).containsExactly(overdueLead);
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(MANAGER_ID, "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }

    private Store store() {
        Instant now = Instant.parse("2026-07-26T10:00:00Z");
        return new Store(STORE_ID, COMPANY_ID, "Loja Centro", "12345678000190", null, null, null, null, null, TenantStatus.ACTIVE, now, now);
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId, Instant assignedAt, Instant createdAt, Instant firstContactAt) {
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
                status,
                assignedToUserId,
                assignedAt,
                createdAt,
                createdAt,
                firstContactAt,
                firstContactAt,
                null,
                null
        );
    }

    private User seller(UUID id, UserStatus status) {
        Instant now = Instant.parse("2026-07-26T10:00:00Z");
        return new User(id, "Vendedor", "seller-" + id + "@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, status, Set.of(UserRole.SELLER), now, now);
    }
}
