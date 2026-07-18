package com.eai.api.settings;

import com.eai.api.distribution.LeadDistributionConfigResponse;
import com.eai.api.tenant.CompanyResponse;
import com.eai.api.tenant.StoreResponse;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.settings.SettingsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse getSettings(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return SettingsResponse.fromSnapshot(settingsService.getSettings(companyId, storeId, authenticatedUser));
    }

    @PutMapping("/company")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyResponse updateCompany(
            @Valid @RequestBody SettingsCompanyRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return CompanyResponse.fromDomain(settingsService.updateCompany(
                request.companyId(),
                request.name(),
                request.status(),
                authenticatedUser
        ));
    }

    @PutMapping("/store")
    public StoreResponse updateStore(
            @Valid @RequestBody SettingsStoreRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return StoreResponse.fromDomain(settingsService.updateStore(
                request.storeId(),
                request.companyId(),
                request.name(),
                request.document(),
                request.email(),
                request.phone(),
                request.city(),
                request.state(),
                request.address(),
                request.status(),
                authenticatedUser
        ));
    }

    @PutMapping("/distribution")
    public LeadDistributionConfigResponse updateDistribution(
            @Valid @RequestBody SettingsDistributionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LeadDistributionConfigResponse.fromSettings(settingsService.updateDistribution(
                request.companyId(),
                request.storeId(),
                request.mode(),
                request.active(),
                authenticatedUser
        ));
    }

    @PutMapping("/sla")
    public LeadDistributionConfigResponse updateSla(
            @Valid @RequestBody SettingsSlaRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LeadDistributionConfigResponse.fromSettings(settingsService.updateSla(
                request.companyId(),
                request.storeId(),
                request.minutesToAssign(),
                request.minutesToFirstContact(),
                request.active(),
                authenticatedUser
        ));
    }
}
