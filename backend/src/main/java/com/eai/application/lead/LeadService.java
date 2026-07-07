package com.eai.application.lead;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.application.user.UserRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.lead.LeadNote;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import com.eai.domain.lead.LeadTag;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final LeadNoteRepository noteRepository;
    private final LeadTagRepository tagRepository;
    private final CompanyService companyService;
    private final StoreService storeService;
    private final UserRepository userRepository;

    public LeadService(
            LeadRepository leadRepository,
            LeadHistoryRepository historyRepository,
            LeadNoteRepository noteRepository,
            LeadTagRepository tagRepository,
            CompanyService companyService,
            StoreService storeService,
            UserRepository userRepository
    ) {
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
        this.companyService = companyService;
        this.storeService = storeService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Lead> listLeads(LeadSearchCriteria criteria, int page, int size, AuthenticatedUser authenticatedUser) {
        LeadSearchCriteria scopedCriteria = applyScope(criteria, authenticatedUser);
        return leadRepository.search(scopedCriteria, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @Transactional(readOnly = true)
    public Lead getLead(UUID id, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        return lead;
    }

    @Transactional
    public Lead createLead(CreateLeadCommand command, AuthenticatedUser authenticatedUser) {
        validateTenant(command.companyId(), command.storeId());
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        if (command.assignedToUserId() != null) {
            assertCanAssignUser(command.assignedToUserId(), command.companyId(), command.storeId(), authenticatedUser);
        }
        LeadSource source = command.source() == null ? LeadSource.MANUAL : command.source();
        Lead lead = Lead.create(
                command.companyId(),
                command.storeId(),
                command.customerName(),
                command.customerPhone(),
                command.customerEmail(),
                command.customerCity(),
                command.vehicleInterest(),
                source,
                command.originalMessage(),
                command.assignedToUserId(),
                command.lostReason(),
                command.saleValue()
        );
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), null, savedLead.getStatus(), "Lead created"));
        return savedLead;
    }

    @Transactional
    public Lead updateLead(UUID id, UpdateLeadCommand command, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        validateTenant(command.companyId(), command.storeId());
        assertCanUseTenant(command.companyId(), command.storeId(), authenticatedUser);
        if (command.assignedToUserId() != null) {
            assertCanAssignUser(command.assignedToUserId(), command.companyId(), command.storeId(), authenticatedUser);
        }
        LeadStatus previousStatus = lead.getStatus();
        Instant assignedAt = command.assignedToUserId() == null
                ? null
                : command.assignedToUserId().equals(lead.getAssignedToUserId()) ? lead.getAssignedAt() : Instant.now();
        lead.update(
                command.companyId(),
                command.storeId(),
                command.customerName(),
                command.customerPhone(),
                command.customerEmail(),
                command.customerCity(),
                command.vehicleInterest(),
                command.source(),
                command.originalMessage(),
                command.status(),
                command.assignedToUserId(),
                assignedAt,
                command.firstContactAt(),
                command.lastContactAt(),
                command.lostReason(),
                command.saleValue()
        );
        Lead savedLead = leadRepository.save(lead);
        if (previousStatus != savedLead.getStatus()) {
            historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Lead updated"));
        }
        return savedLead;
    }

    @Transactional
    public Lead changeStatus(UUID id, LeadStatus status, String description, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        LeadStatus previousStatus = lead.changeStatus(status);
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), description));
        return savedLead;
    }

    @Transactional
    public Lead assignToMe(UUID id, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        if (lead.getAssignedToUserId() != null && !lead.getAssignedToUserId().equals(authenticatedUser.id())) {
            throw new ConflictException("Lead already assigned");
        }
        return assign(id, authenticatedUser.id(), authenticatedUser);
    }

    @Transactional
    public Lead assign(UUID id, UUID userId, AuthenticatedUser authenticatedUser) {
        Lead lead = findRequired(id);
        assertCanAccessLead(lead, authenticatedUser);
        assertCanAssignUser(userId, lead.getCompanyId(), lead.getStoreId(), authenticatedUser);
        LeadStatus previousStatus = lead.assignTo(userId);
        Lead savedLead = leadRepository.save(lead);
        historyRepository.save(LeadHistory.create(savedLead.getId(), authenticatedUser.id(), previousStatus, savedLead.getStatus(), "Lead assigned"));
        return savedLead;
    }

    @Transactional
    public LeadNote addNote(UUID leadId, String note, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return noteRepository.save(LeadNote.create(lead.getId(), authenticatedUser.id(), note));
    }

    @Transactional(readOnly = true)
    public List<LeadHistory> listHistory(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return historyRepository.findByLeadId(lead.getId());
    }

    @Transactional(readOnly = true)
    public List<LeadNote> listNotes(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return noteRepository.findByLeadId(lead.getId());
    }

    @Transactional
    public LeadTag addTag(UUID leadId, String name, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return tagRepository.save(LeadTag.create(lead.getId(), name));
    }

    @Transactional(readOnly = true)
    public List<LeadTag> listTags(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        return tagRepository.findByLeadId(lead.getId());
    }

    @Transactional
    public void deleteTag(UUID leadId, UUID tagId, AuthenticatedUser authenticatedUser) {
        Lead lead = getLead(leadId, authenticatedUser);
        LeadTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Lead tag not found"));
        if (!tag.getLeadId().equals(lead.getId())) {
            throw new NotFoundException("Lead tag not found");
        }
        tagRepository.deleteById(tag.getId());
    }

    private Lead findRequired(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private LeadSearchCriteria applyScope(LeadSearchCriteria criteria, AuthenticatedUser authenticatedUser) {
        UUID scopeCompanyId = null;
        UUID scopeStoreId = null;
        if (!hasRole(authenticatedUser, UserRole.ADMIN)) {
            scopeCompanyId = requireCompany(authenticatedUser);
            scopeStoreId = requireStore(authenticatedUser);
        }
        return new LeadSearchCriteria(
                criteria.status(),
                criteria.source(),
                criteria.assignedToUserId(),
                criteria.storeId(),
                criteria.createdFrom(),
                criteria.createdTo(),
                criteria.text(),
                criteria.vehicle(),
                criteria.phone(),
                scopeCompanyId,
                scopeStoreId
        );
    }

    private void validateTenant(UUID companyId, UUID storeId) {
        if (companyId == null || storeId == null) {
            throw new IllegalArgumentException("companyId and storeId are required");
        }
        companyService.findRequired(companyId);
        Store store = storeService.findRequired(storeId);
        if (!store.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
    }

    private void assertCanAccessLead(Lead lead, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (lead.getCompanyId().equals(requireCompany(authenticatedUser)) && lead.getStoreId().equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for lead");
    }

    private void assertCanUseTenant(UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (companyId.equals(requireCompany(authenticatedUser)) && storeId.equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for lead tenant");
    }

    private void assertCanAssignUser(UUID userId, UUID companyId, UUID storeId, AuthenticatedUser authenticatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));
        if (!companyId.equals(user.getCompanyId()) || !storeId.equals(user.getStoreId())) {
            throw new ForbiddenException("Assigned user does not belong to lead store");
        }
        if (hasRole(authenticatedUser, UserRole.ADMIN) || userId.equals(authenticatedUser.id())) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER) && companyId.equals(requireCompany(authenticatedUser)) && storeId.equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for assignment");
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

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
