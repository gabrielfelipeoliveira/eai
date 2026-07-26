package com.eai.application.distribution;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadDistributionServiceAdditionalTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID OTHER_SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final Instant NOW = Instant.parse("2026-07-26T10:00:00Z");

    private final LeadDistributionConfigRepository configRepository = mock(LeadDistributionConfigRepository.class);
    private final LeadSlaPolicyRepository slaPolicyRepository = mock(LeadSlaPolicyRepository.class);
    private final LeadRepository leadRepository = mock(LeadRepository.class);
    private final LeadHistoryRepository historyRepository = mock(LeadHistoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StoreService storeService = mock(StoreService.class);
    private final LeadDistributionService service = new LeadDistributionService(
            configRepository,
            slaPolicyRepository,
            leadRepository,
            historyRepository,
            userRepository,
            storeService,
            new ManualAssignmentStrategy(),
            new RoundRobinAssignmentStrategy(leadRepository),
            new LeastBusySellerAssignmentStrategy(leadRepository)
    );

    @DisplayName("Busca configuracoes com valores padrao quando loja nao possui cadastro")
    @Test
    void getSettingsReturnsDefaultsWhenStoreHasNoSettings() {
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID)).thenReturn(Optional.empty());
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID)).thenReturn(Optional.empty());

        LeadDistributionSettings settings = service.getSettings(COMPANY_ID, STORE_ID, manager());

        assertThat(settings.distributionConfig().getMode()).isEqualTo(LeadDistributionMode.MANUAL);
        assertThat(settings.distributionConfig().isActive()).isFalse();
        assertThat(settings.slaPolicy().getMinutesToAssign()).isEqualTo(15);
        assertThat(settings.slaPolicy().getMinutesToFirstContact()).isEqualTo(30);
        assertThat(settings.slaPolicy().isActive()).isFalse();
    }

    @DisplayName("Nao permite gerente consultar configuracao de empresa diferente")
    @Test
    void managerCannotReadSettingsFromAnotherCompany() {
        when(storeService.findRequired(STORE_ID)).thenReturn(store(OTHER_COMPANY_ID));

        assertThatThrownBy(() -> service.getSettings(OTHER_COMPANY_ID, STORE_ID, manager()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("Distribui leads pendentes apenas para vendedores ativos")
    @Test
    void distributePendingAssignsOnlyToActiveSellers() {
        Lead firstLead = lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(3600), null);
        Lead secondLead = lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(1800), null);
        User activeSeller = seller(SELLER_ID, UserStatus.ACTIVE);
        User inactiveSeller = seller(OTHER_SELLER_ID, UserStatus.INACTIVE);
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadDistributionConfig.create(COMPANY_ID, STORE_ID, LeadDistributionMode.ROUND_ROBIN, true)));
        when(leadRepository.findPendingByStoreId(STORE_ID)).thenReturn(List.of(firstLead, secondLead));
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(activeSeller, inactiveSeller));
        when(leadRepository.findMostRecentAssignedSellerId(STORE_ID)).thenReturn(Optional.empty());
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(LeadHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Lead> distributed = service.distributePending(manager());

        assertThat(distributed).hasSize(2);
        assertThat(distributed).extracting(Lead::getAssignedToUserId).containsOnly(SELLER_ID);
        assertThat(distributed).extracting(Lead::getStatus).containsOnly(LeadStatus.ASSIGNED);
        verify(historyRepository, times(2)).save(any(LeadHistory.class));
    }

    @DisplayName("Nao distribui automaticamente quando configuracao esta manual")
    @Test
    void distributePendingRejectsManualConfiguration() {
        Lead lead = lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(3600), null);
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(leadRepository.findPendingByStoreId(STORE_ID)).thenReturn(List.of(lead));
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadDistributionConfig.create(COMPANY_ID, STORE_ID, LeadDistributionMode.MANUAL, true)));

        assertThatThrownBy(() -> service.distributePending(manager()))
                .isInstanceOf(ConflictException.class);

        verify(leadRepository, never()).save(any(Lead.class));
    }

    @DisplayName("Nao atribui automaticamente quando nao ha vendedor ativo")
    @Test
    void assignAutomaticallyRejectsWhenNoActiveSellerExists() {
        Lead lead = lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(3600), null);
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(configRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadDistributionConfig.create(COMPANY_ID, STORE_ID, LeadDistributionMode.ROUND_ROBIN, true)));
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(seller(SELLER_ID, UserStatus.INACTIVE)));

        assertThatThrownBy(() -> service.assignAutomatically(lead.getId(), manager()))
                .isInstanceOf(ConflictException.class);

        verify(leadRepository, never()).save(any(Lead.class));
    }

    @DisplayName("Dashboard contabiliza leads sem responsavel vencidos e carga por vendedor")
    @Test
    void dashboardReturnsUnassignedOverdueAndSellerWorkload() {
        Lead unassignedOverdue = lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(3600), null);
        Lead contactedOverdue = lead(LeadStatus.ASSIGNED, SELLER_ID, NOW.minusSeconds(3600), null);
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(leadRepository.findOverdueCandidatesByStoreId(STORE_ID)).thenReturn(List.of(unassignedOverdue, contactedOverdue));
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true)));
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of(
                seller(SELLER_ID, UserStatus.ACTIVE),
                seller(OTHER_SELLER_ID, UserStatus.INACTIVE)
        ));
        when(leadRepository.countOpenByAssignedToUserId(SELLER_ID)).thenReturn(4L);

        LeadDashboardMetrics metrics = service.dashboard(manager());

        assertThat(metrics.unassignedLeads()).isEqualTo(1);
        assertThat(metrics.overdueLeads()).isEqualTo(2);
        assertThat(metrics.leadsBySeller()).singleElement()
                .satisfies(sellerMetrics -> {
                    assertThat(sellerMetrics.sellerId()).isEqualTo(SELLER_ID);
                    assertThat(sellerMetrics.sellerName()).isEqualTo("Vendedor");
                    assertThat(sellerMetrics.leadCount()).isEqualTo(4);
                });
    }

    @DisplayName("Dashboard ignora vencimentos quando SLA esta inativo")
    @Test
    void dashboardIgnoresOverdueWhenSlaIsInactive() {
        when(storeService.findRequired(STORE_ID)).thenReturn(store(COMPANY_ID));
        when(leadRepository.findOverdueCandidatesByStoreId(STORE_ID))
                .thenReturn(List.of(lead(LeadStatus.AVAILABLE, null, NOW.minusSeconds(3600), null)));
        when(slaPolicyRepository.findByCompanyIdAndStoreId(COMPANY_ID, STORE_ID))
                .thenReturn(Optional.of(LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, false)));
        when(userRepository.findByStoreId(STORE_ID)).thenReturn(List.of());

        LeadDashboardMetrics metrics = service.dashboard(manager());

        assertThat(metrics.unassignedLeads()).isEqualTo(1);
        assertThat(metrics.overdueLeads()).isZero();
        assertThat(metrics.leadsBySeller()).isEmpty();
    }

    private Store store(UUID companyId) {
        return new Store(STORE_ID, companyId, "Loja Centro", "12345678000190", null, null, null, null, null, TenantStatus.ACTIVE, NOW, NOW);
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId, Instant createdAt, Instant firstContactAt) {
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
                assignedToUserId == null ? null : createdAt,
                createdAt,
                createdAt,
                firstContactAt,
                null,
                null,
                null
        );
    }

    private User seller(UUID id, UserStatus status) {
        return new User(id, "Vendedor", "seller-" + id + "@eai.com", "hash", null, null, COMPANY_ID, STORE_ID, status, Set.of(UserRole.SELLER), NOW, NOW);
    }

    private AuthenticatedUser manager() {
        return new AuthenticatedUser(MANAGER_ID, "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));
    }
}
