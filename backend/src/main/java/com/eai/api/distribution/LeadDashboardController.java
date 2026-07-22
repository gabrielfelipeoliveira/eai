package com.eai.api.distribution;

import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
@RequiredArgsConstructor
public class LeadDashboardController {

    private final LeadDistributionService distributionService;

    @GetMapping("/leads")
    public LeadDashboardResponse getLeadDashboard(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return LeadDashboardResponse.fromMetrics(distributionService.dashboard(authenticatedUser));
    }
}
