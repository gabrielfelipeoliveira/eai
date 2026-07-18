package com.eai.application.settings;

import com.eai.application.common.ForbiddenException;
import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.distribution.LeadDistributionSettings;
import com.eai.application.distribution.UpdateLeadDistributionSettingsCommand;
import com.eai.application.email.EmailAccountService;
import com.eai.application.message.MessageTemplateService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.application.tenant.UpdateCompanyCommand;
import com.eai.application.tenant.UpdateStoreCommand;
import com.eai.application.user.UserService;
import com.eai.domain.distribution.LeadDistributionMode;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SettingsService {

    private final CompanyService companyService;
    private final StoreService storeService;
    private final LeadDistributionService distributionService;
    private final UserService userService;
    private final MessageTemplateService templateService;
    private final EmailAccountService emailAccountService;

    public SettingsService(
            CompanyService companyService,
            StoreService storeService,
            LeadDistributionService distributionService,
            UserService userService,
            MessageTemplateService templateService,
            EmailAccountService emailAccountService
    ) {
        this.companyService = companyService;
        this.storeService = storeService;
        this.distributionService = distributionService;
        this.userService = userService;
        this.templateService = templateService;
        this.emailAccountService = emailAccountService;
    }

    @Transactional(readOnly = true)
    public SettingsSnapshot getSettings(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        assertCanAccessSettings(authenticatedUser);
        TenantScope scope = resolveScope(companyId, storeId, authenticatedUser);
        Company company = companyService.findRequired(scope.companyId());
        Store store = storeService.getStore(scope.storeId(), authenticatedUser);
        LeadDistributionSettings distributionSettings = distributionService.getSettings(scope.companyId(), scope.storeId(), authenticatedUser);
        List<Company> availableCompanies = hasRole(authenticatedUser, UserRole.ADMIN)
                ? companyService.listCompanies()
                : List.of(company);
        return new SettingsSnapshot(
                company,
                store,
                distributionSettings,
                availableCompanies,
                storeService.listStores(authenticatedUser),
                userService.listUsers(authenticatedUser),
                templateService.listTemplates(authenticatedUser),
                emailAccountService.listAccounts(authenticatedUser),
                SystemPreferences.defaults()
        );
    }

    @Transactional
    public Company updateCompany(UUID companyId, String name, TenantStatus status, AuthenticatedUser authenticatedUser) {
        if (!hasRole(authenticatedUser, UserRole.ADMIN)) {
            throw new ForbiddenException("Access denied for company settings");
        }
        UUID resolvedCompanyId = companyId == null ? requireCompany(authenticatedUser) : companyId;
        return companyService.updateCompany(resolvedCompanyId, new UpdateCompanyCommand(name, status));
    }

    @Transactional
    public Store updateStore(UUID storeId, UUID companyId, String name, String document, String email, String phone, String city, String state, String address, TenantStatus status, AuthenticatedUser authenticatedUser) {
        assertCanAccessSettings(authenticatedUser);
        UUID resolvedStoreId = storeId == null ? requireStore(authenticatedUser) : storeId;
        Store currentStore = storeService.getStore(resolvedStoreId, authenticatedUser);
        UUID resolvedCompanyId = companyId == null ? currentStore.getCompanyId() : companyId;
        return storeService.updateStore(resolvedStoreId, new UpdateStoreCommand(
                resolvedCompanyId,
                name,
                document,
                email,
                phone,
                city,
                state,
                address,
                status
        ), authenticatedUser);
    }

    @Transactional
    public LeadDistributionSettings updateDistribution(UUID companyId, UUID storeId, LeadDistributionMode mode, boolean active, AuthenticatedUser authenticatedUser) {
        assertCanAccessSettings(authenticatedUser);
        TenantScope scope = resolveScope(companyId, storeId, authenticatedUser);
        LeadDistributionSettings current = distributionService.getSettings(scope.companyId(), scope.storeId(), authenticatedUser);
        return distributionService.updateSettings(new UpdateLeadDistributionSettingsCommand(
                scope.companyId(),
                scope.storeId(),
                mode,
                active,
                current.slaPolicy().getMinutesToAssign(),
                current.slaPolicy().getMinutesToFirstContact(),
                current.slaPolicy().isActive()
        ), authenticatedUser);
    }

    @Transactional
    public LeadDistributionSettings updateSla(UUID companyId, UUID storeId, int minutesToAssign, int minutesToFirstContact, boolean active, AuthenticatedUser authenticatedUser) {
        assertCanAccessSettings(authenticatedUser);
        TenantScope scope = resolveScope(companyId, storeId, authenticatedUser);
        LeadDistributionSettings current = distributionService.getSettings(scope.companyId(), scope.storeId(), authenticatedUser);
        return distributionService.updateSettings(new UpdateLeadDistributionSettingsCommand(
                scope.companyId(),
                scope.storeId(),
                current.distributionConfig().getMode(),
                current.distributionConfig().isActive(),
                minutesToAssign,
                minutesToFirstContact,
                active
        ), authenticatedUser);
    }

    private TenantScope resolveScope(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        UUID resolvedCompanyId = companyId == null ? defaultCompany(authenticatedUser) : companyId;
        UUID resolvedStoreId = storeId == null ? defaultStore(resolvedCompanyId, authenticatedUser) : storeId;
        Store store = storeService.getStore(resolvedStoreId, authenticatedUser);
        if (!store.getCompanyId().equals(resolvedCompanyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
        return new TenantScope(resolvedCompanyId, resolvedStoreId);
    }

    private void assertCanAccessSettings(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN) || hasRole(authenticatedUser, UserRole.MANAGER)) {
            return;
        }
        throw new ForbiddenException("Access denied for administrative settings");
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private UUID requireStore(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() == null) {
            throw new ForbiddenException("User is not linked to a store");
        }
        return authenticatedUser.storeId();
    }

    private UUID defaultCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() != null) {
            return authenticatedUser.companyId();
        }
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return companyService.listCompanies().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No company available"))
                    .getId();
        }
        return requireCompany(authenticatedUser);
    }

    private UUID defaultStore(UUID companyId, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() != null) {
            return authenticatedUser.storeId();
        }
        return storeService.listStoresByCompany(companyId, authenticatedUser).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No store available for company"))
                .getId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }

    private record TenantScope(UUID companyId, UUID storeId) {
    }
}
