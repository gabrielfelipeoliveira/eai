package com.eai.application.lead;

import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PipelineService {

    private final LeadRepository leadRepository;

    public PipelineService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Transactional(readOnly = true)
    public Map<LeadStatus, List<Lead>> getPipeline(AuthenticatedUser authenticatedUser) {
        LeadSearchCriteria criteria = pipelineScope(authenticatedUser);
        List<Lead> leads = leadRepository.findAll(criteria);
        Map<LeadStatus, List<Lead>> grouped = new EnumMap<>(LeadStatus.class);
        for (LeadStatus status : LeadStatus.values()) {
            grouped.put(status, leads.stream()
                    .filter(lead -> lead.getStatus() == status)
                    .toList());
        }
        return grouped;
    }

    private LeadSearchCriteria pipelineScope(AuthenticatedUser authenticatedUser) {
        UUID scopeCompanyId = null;
        UUID scopeStoreId = null;
        UUID assignedToUserId = null;
        if (!hasRole(authenticatedUser, UserRole.ADMIN)) {
            scopeCompanyId = authenticatedUser.companyId();
            scopeStoreId = authenticatedUser.storeId();
        }
        if (hasRole(authenticatedUser, UserRole.SELLER) && !hasRole(authenticatedUser, UserRole.MANAGER) && !hasRole(authenticatedUser, UserRole.ADMIN)) {
            assignedToUserId = authenticatedUser.id();
        }
        return new LeadSearchCriteria(null, null, assignedToUserId, null, null, null, null, null, null, scopeCompanyId, scopeStoreId);
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
