package com.eai.api.lead;

import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.lead.PipelineService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER')")
public class PipelineController {

    private final PipelineService pipelineService;
    private final LeadDistributionService distributionService;

    public PipelineController(PipelineService pipelineService, LeadDistributionService distributionService) {
        this.pipelineService = pipelineService;
        this.distributionService = distributionService;
    }

    @GetMapping
    public Map<LeadStatus, List<LeadResponse>> getPipeline(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return pipelineService.getPipeline(authenticatedUser).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(this::toResponse)
                        .toList()));
    }

    private LeadResponse toResponse(Lead lead) {
        LeadSlaPolicy policy = distributionService.findOrDefaultSla(lead.getCompanyId(), lead.getStoreId());
        if (!policy.isActive()) {
            return LeadResponse.fromDomain(lead);
        }
        return LeadResponse.fromDomain(lead, policy.getMinutesToAssign(), policy.getMinutesToFirstContact(), Instant.now());
    }
}
