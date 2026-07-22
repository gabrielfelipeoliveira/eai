package com.eai.application.lgpd;

import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.lead.LeadRepository;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.domain.lead.Lead;
import com.eai.domain.lgpd.LgpdRequest;
import com.eai.domain.lgpd.LgpdRequestAction;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LgpdRequestService {

    private final LgpdRequestRepository requestRepository;
    private final LgpdRequestActionRepository actionRepository;
    private final CompanyService companyService;
    private final StoreService storeService;
    private final LeadRepository leadRepository;

    public LgpdRequestService(
            LgpdRequestRepository requestRepository,
            LgpdRequestActionRepository actionRepository,
            CompanyService companyService,
            StoreService storeService,
            LeadRepository leadRepository
    ) {
        this.requestRepository = requestRepository;
        this.actionRepository = actionRepository;
        this.companyService = companyService;
        this.storeService = storeService;
        this.leadRepository = leadRepository;
    }

    @Transactional
    public LgpdRequest create(CreateLgpdRequestCommand command, AuthenticatedUser authenticatedUser) {
        assertAdmin(authenticatedUser);
        validateReferences(command.companyId(), command.storeId(), command.leadId());
        return requestRepository.save(LgpdRequest.create(
                command.companyId(),
                command.storeId(),
                command.leadId(),
                command.dataSubjectName(),
                command.dataSubjectPhone(),
                command.dataSubjectEmail(),
                command.requestType(),
                command.description(),
                authenticatedUser.id()
        ));
    }

    @Transactional(readOnly = true)
    public LgpdRequestPageResult<LgpdRequest> list(LgpdRequestSearchCriteria criteria, int page, int size, AuthenticatedUser authenticatedUser) {
        assertAdmin(authenticatedUser);
        return requestRepository.search(criteria, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @Transactional(readOnly = true)
    public LgpdRequestDetails get(UUID id, AuthenticatedUser authenticatedUser) {
        assertAdmin(authenticatedUser);
        LgpdRequest request = findRequired(id);
        return new LgpdRequestDetails(request, actionRepository.findByRequestId(request.getId()));
    }

    @Transactional
    public LgpdRequestDetails registerAction(UUID id, RegisterLgpdRequestActionCommand command, AuthenticatedUser authenticatedUser) {
        assertAdmin(authenticatedUser);
        LgpdRequest request = findRequired(id);
        LgpdRequestAction action = LgpdRequestAction.create(
                request.getId(),
                authenticatedUser.id(),
                command.actionType(),
                command.resolution(),
                command.finalStatus()
        );
        request.registerAction(command.finalStatus());
        requestRepository.save(request);
        actionRepository.save(action);
        return new LgpdRequestDetails(request, actionRepository.findByRequestId(request.getId()));
    }

    private LgpdRequest findRequired(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LGPD request not found"));
    }

    private void validateReferences(UUID companyId, UUID storeId, UUID leadId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        companyService.findRequired(companyId);
        if (storeId != null) {
            Store store = storeService.findRequired(storeId);
            if (!store.getCompanyId().equals(companyId)) {
                throw new IllegalArgumentException("store does not belong to company");
            }
        }
        if (leadId != null) {
            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new NotFoundException("Lead not found"));
            if (!lead.getCompanyId().equals(companyId)) {
                throw new IllegalArgumentException("lead does not belong to company");
            }
            if (storeId != null && !lead.getStoreId().equals(storeId)) {
                throw new IllegalArgumentException("lead does not belong to store");
            }
        }
    }

    private void assertAdmin(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || !authenticatedUser.roles().contains(UserRole.ADMIN)) {
            throw new ForbiddenException("Only ADMIN can manage LGPD requests");
        }
    }
}
