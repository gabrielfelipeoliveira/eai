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
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LeadDistributionService {

    private static final int DEFAULT_MINUTES_TO_ASSIGN = 15;
    private static final int DEFAULT_MINUTES_TO_FIRST_CONTACT = 30;

    private final LeadDistributionConfigRepository configRepository;
    private final LeadSlaPolicyRepository slaPolicyRepository;
    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final StoreService storeService;
    private final ManualAssignmentStrategy manualAssignmentStrategy;
    private final RoundRobinAssignmentStrategy roundRobinAssignmentStrategy;
    private final LeastBusySellerAssignmentStrategy leastBusySellerAssignmentStrategy;
    private final LeadSlaEvaluator slaEvaluator = new LeadSlaEvaluator();

    public LeadDistributionService(
            LeadDistributionConfigRepository configRepository,
            LeadSlaPolicyRepository slaPolicyRepository,
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            UserRepository userRepository,
            StoreService storeService,
            ManualAssignmentStrategy manualAssignmentStrategy,
            RoundRobinAssignmentStrategy roundRobinAssignmentStrategy,
            LeastBusySellerAssignmentStrategy leastBusySellerAssignmentStrategy
    ) {
        this.configRepository = configRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.storeService = storeService;
        this.manualAssignmentStrategy = manualAssignmentStrategy;
        this.roundRobinAssignmentStrategy = roundRobinAssignmentStrategy;
        this.leastBusySellerAssignmentStrategy = leastBusySellerAssignmentStrategy;
    }

    @Transactional(readOnly = true)
    public LeadDistributionSettings getSettings(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        TenantScope scope = resolveScope(companyId, storeId, authenticatedUser);
        assertCanManageStore(scope.companyId(), scope.storeId(), authenticatedUser);
        return new LeadDistributionSettings(findOrDefaultConfig(scope.companyId(), scope.storeId()), findOrDefaultSla(scope.companyId(), scope.storeId()));
    }

    @Transactional
    public LeadDistributionSettings updateSettings(UpdateLeadDistributionSettingsCommand command, AuthenticatedUser authenticatedUser) {
        TenantScope scope = resolveScope(command.companyId(), command.storeId(), authenticatedUser);
        assertCanManageStore(scope.companyId(), scope.storeId(), authenticatedUser);

        LeadDistributionConfig config = configRepository.findByCompanyIdAndStoreId(scope.companyId(), scope.storeId())
                .orElseGet(() -> LeadDistributionConfig.create(scope.companyId(), scope.storeId(), command.mode(), command.distributionActive()));
        config.update(command.mode(), command.distributionActive());

        LeadSlaPolicy policy = slaPolicyRepository.findByCompanyIdAndStoreId(scope.companyId(), scope.storeId())
                .orElseGet(() -> LeadSlaPolicy.create(
                        scope.companyId(),
                        scope.storeId(),
                        command.minutesToAssign(),
                        command.minutesToFirstContact(),
                        command.slaActive()
                ));
        policy.update(command.minutesToAssign(), command.minutesToFirstContact(), command.slaActive());

        return new LeadDistributionSettings(configRepository.save(config), slaPolicyRepository.save(policy));
    }

    @Transactional
    public Lead assignAutomatically(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new com.eai.application.common.NotFoundException("Lead not found"));
        assertCanAccessStore(lead.getCompanyId(), lead.getStoreId(), authenticatedUser);
        if (lead.getAssignedToUserId() != null) {
            throw new ConflictException("Lead already assigned");
        }
        User seller = selectSeller(lead);
        LeadStatus previousStatus = lead.getStatus();
        lead.assignTo(seller.getId());
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Lead assigned automatically"));
        return savedLead;
    }

    @Transactional
    public List<Lead> distributePending(AuthenticatedUser authenticatedUser) {
        TenantScope scope = resolveScope(null, null, authenticatedUser);
        assertCanManageStore(scope.companyId(), scope.storeId(), authenticatedUser);
        return leadRepository.findPendingByStoreId(scope.storeId()).stream()
                .map(lead -> {
                    User seller = selectSeller(lead);
                    LeadStatus previousStatus = lead.getStatus();
                    lead.assignTo(seller.getId());
                    Lead savedLead = leadRepository.save(lead);
                    historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Pending lead distributed"));
                    return savedLead;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Lead> listOverdue(AuthenticatedUser authenticatedUser) {
        TenantScope scope = resolveScope(null, null, authenticatedUser);
        assertCanAccessStore(scope.companyId(), scope.storeId(), authenticatedUser);
        LeadSlaPolicy policy = findOrDefaultSla(scope.companyId(), scope.storeId());
        if (!policy.isActive()) {
            return List.of();
        }
        Instant now = Instant.now();
        return leadRepository.findOverdueCandidatesByStoreId(scope.storeId()).stream()
                .filter(lead -> slaEvaluator.isOverdueToAssign(lead, policy, now) || slaEvaluator.isOverdueToFirstContact(lead, policy, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public LeadDashboardMetrics dashboard(AuthenticatedUser authenticatedUser) {
        TenantScope scope = resolveScope(null, null, authenticatedUser);
        assertCanAccessStore(scope.companyId(), scope.storeId(), authenticatedUser);
        List<Lead> leads = leadRepository.findOverdueCandidatesByStoreId(scope.storeId());
        LeadSlaPolicy policy = findOrDefaultSla(scope.companyId(), scope.storeId());
        Instant now = Instant.now();
        long unassigned = leads.stream().filter(lead -> lead.getAssignedToUserId() == null).count();
        long overdue = policy.isActive()
                ? leads.stream().filter(lead -> slaEvaluator.isOverdueToAssign(lead, policy, now) || slaEvaluator.isOverdueToFirstContact(lead, policy, now)).count()
                : 0;
        List<LeadDashboardMetrics.LeadsBySeller> bySeller = activeSellers(scope.storeId()).stream()
                .map(seller -> new LeadDashboardMetrics.LeadsBySeller(
                        seller.getId(),
                        seller.getName(),
                        leadRepository.countOpenByAssignedToUserId(seller.getId())
                ))
                .toList();
        return new LeadDashboardMetrics(unassigned, overdue, bySeller);
    }

    public LeadSlaPolicy findOrDefaultSla(UUID companyId, UUID storeId) {
        return slaPolicyRepository.findByCompanyIdAndStoreId(companyId, storeId)
                .orElseGet(() -> LeadSlaPolicy.create(companyId, storeId, DEFAULT_MINUTES_TO_ASSIGN, DEFAULT_MINUTES_TO_FIRST_CONTACT, false));
    }

    private User selectSeller(Lead lead) {
        LeadDistributionConfig config = findOrDefaultConfig(lead.getCompanyId(), lead.getStoreId());
        if (!config.isActive() || config.getMode() == LeadDistributionMode.MANUAL) {
            throw new ConflictException("Automatic distribution is not active for this store");
        }
        List<User> sellers = activeSellers(lead.getStoreId());
        return strategy(config.getMode()).selectSeller(lead, sellers)
                .orElseThrow(() -> new ConflictException("No active seller available for lead distribution"));
    }

    private LeadDistributionConfig findOrDefaultConfig(UUID companyId, UUID storeId) {
        return configRepository.findByCompanyIdAndStoreId(companyId, storeId)
                .orElseGet(() -> LeadDistributionConfig.create(companyId, storeId, LeadDistributionMode.MANUAL, false));
    }

    private LeadAssignmentStrategy strategy(LeadDistributionMode mode) {
        return switch (mode) {
            case MANUAL -> manualAssignmentStrategy;
            case ROUND_ROBIN -> roundRobinAssignmentStrategy;
            case LEAST_BUSY -> leastBusySellerAssignmentStrategy;
        };
    }

    private List<User> activeSellers(UUID storeId) {
        return userRepository.findByStoreId(storeId).stream()
                .filter(User::isActive)
                .filter(user -> user.getRoles().contains(UserRole.SELLER))
                .toList();
    }

    private TenantScope resolveScope(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        UUID resolvedCompanyId = companyId == null ? authenticatedUser.companyId() : companyId;
        UUID resolvedStoreId = storeId == null ? authenticatedUser.storeId() : storeId;
        if (resolvedCompanyId == null || resolvedStoreId == null) {
            throw new ForbiddenException("Company and store scope are required");
        }
        Store store = storeService.findRequired(resolvedStoreId);
        if (!store.getCompanyId().equals(resolvedCompanyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
        return new TenantScope(resolvedCompanyId, resolvedStoreId);
    }

    private void assertCanManageStore(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(authenticatedUser.companyId())) {
            return;
        }
        throw new ForbiddenException("Access denied for lead distribution settings");
    }

    private void assertCanAccessStore(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(authenticatedUser.companyId())) {
            return;
        }
        if (storeId.equals(authenticatedUser.storeId())) {
            return;
        }
        throw new ForbiddenException("Access denied for lead distribution");
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }

    private record TenantScope(UUID companyId, UUID storeId) {
    }
}
