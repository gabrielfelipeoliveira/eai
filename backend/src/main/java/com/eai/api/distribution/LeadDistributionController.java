package com.eai.api.distribution;

import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.distribution.UpdateLeadDistributionSettingsCommand;
import com.eai.application.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/distribution")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequiredArgsConstructor
public class LeadDistributionController {

    private final LeadDistributionService distributionService;

    @GetMapping("/config")
    public LeadDistributionConfigResponse getConfig(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LeadDistributionConfigResponse.fromSettings(distributionService.getSettings(companyId, storeId, authenticatedUser));
    }

    @PutMapping("/config")
    public LeadDistributionConfigResponse updateConfig(
            @Valid @RequestBody LeadDistributionConfigRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return LeadDistributionConfigResponse.fromSettings(distributionService.updateSettings(new UpdateLeadDistributionSettingsCommand(
                request.companyId(),
                request.storeId(),
                request.mode(),
                request.active(),
                request.minutesToAssign(),
                request.minutesToFirstContact(),
                request.slaActive()
        ), authenticatedUser));
    }
}
