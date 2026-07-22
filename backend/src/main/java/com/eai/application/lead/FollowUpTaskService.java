package com.eai.application.lead;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.user.UserRepository;
import com.eai.domain.lead.FollowUpTask;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadHistory;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowUpTaskService {

    private final FollowUpTaskRepository taskRepository;
    private final LeadRepository leadRepository;
    private final LeadHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowUpTask create(UUID leadId, CreateFollowUpTaskCommand command, AuthenticatedUser authenticatedUser) {
        Lead lead = findLeadRequired(leadId);
        assertCanAccessLead(lead, authenticatedUser);
        UUID ownerId = command.userId() == null ? defaultOwnerId(lead, authenticatedUser) : command.userId();
        assertCanAssignTask(ownerId, lead, authenticatedUser);
        FollowUpTask savedTask = taskRepository.save(FollowUpTask.create(lead.getId(), ownerId, command.title(), command.description(), command.dueAt()));
        historyRepository.save(LeadHistory.create(lead.getId(), authenticatedUser.id(), lead.getStatus(), lead.getStatus(), "Follow-up created: " + savedTask.getTitle()));
        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<FollowUpTask> list(AuthenticatedUser authenticatedUser) {
        return taskRepository.findVisible(scopeCompanyId(authenticatedUser), scopeStoreId(authenticatedUser), scopeUserId(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public List<FollowUpTask> listMy(AuthenticatedUser authenticatedUser) {
        return taskRepository.findVisible(scopeCompanyId(authenticatedUser), scopeStoreId(authenticatedUser), authenticatedUser.id());
    }

    @Transactional(readOnly = true)
    public List<FollowUpTask> listByLead(UUID leadId, AuthenticatedUser authenticatedUser) {
        Lead lead = findLeadRequired(leadId);
        assertCanAccessLead(lead, authenticatedUser);
        return taskRepository.findByLeadId(lead.getId());
    }

    @Transactional
    public FollowUpTask complete(UUID id, AuthenticatedUser authenticatedUser) {
        FollowUpTask task = findTaskRequired(id);
        Lead lead = findLeadRequired(task.getLeadId());
        assertCanAccessTask(task, lead, authenticatedUser);
        try {
            task.complete();
        } catch (IllegalStateException exception) {
            throw new ConflictException(exception.getMessage());
        }
        FollowUpTask savedTask = taskRepository.save(task);
        historyRepository.save(LeadHistory.create(lead.getId(), authenticatedUser.id(), lead.getStatus(), lead.getStatus(), "Follow-up completed: " + savedTask.getTitle()));
        return savedTask;
    }

    @Transactional
    public FollowUpTask cancel(UUID id, AuthenticatedUser authenticatedUser) {
        FollowUpTask task = findTaskRequired(id);
        Lead lead = findLeadRequired(task.getLeadId());
        assertCanAccessTask(task, lead, authenticatedUser);
        try {
            task.cancel();
        } catch (IllegalStateException exception) {
            throw new ConflictException(exception.getMessage());
        }
        FollowUpTask savedTask = taskRepository.save(task);
        historyRepository.save(LeadHistory.create(lead.getId(), authenticatedUser.id(), lead.getStatus(), lead.getStatus(), "Follow-up canceled: " + savedTask.getTitle()));
        return savedTask;
    }

    private Lead findLeadRequired(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private FollowUpTask findTaskRequired(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Follow-up not found"));
    }

    private UUID defaultOwnerId(Lead lead, AuthenticatedUser authenticatedUser) {
        if (lead.getAssignedToUserId() != null) {
            return lead.getAssignedToUserId();
        }
        return authenticatedUser.id();
    }

    private void assertCanAccessTask(FollowUpTask task, Lead lead, AuthenticatedUser authenticatedUser) {
        assertCanAccessLead(lead, authenticatedUser);
        if (hasRole(authenticatedUser, UserRole.SELLER) && !task.getUserId().equals(authenticatedUser.id())) {
            throw new ForbiddenException("Access denied for follow-up");
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

    private void assertCanAssignTask(UUID userId, Lead lead, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.SELLER) && !userId.equals(authenticatedUser.id())) {
            throw new ForbiddenException("Seller can only create own follow-ups");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Follow-up user not found"));
        if (!lead.getCompanyId().equals(user.getCompanyId()) || !lead.getStoreId().equals(user.getStoreId())) {
            throw new ForbiddenException("Follow-up user does not belong to lead store");
        }
    }

    private UUID scopeCompanyId(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return null;
        }
        return requireCompany(authenticatedUser);
    }

    private UUID scopeStoreId(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return null;
        }
        return requireStore(authenticatedUser);
    }

    private UUID scopeUserId(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.SELLER) && !hasRole(authenticatedUser, UserRole.MANAGER) && !hasRole(authenticatedUser, UserRole.ADMIN)) {
            return authenticatedUser.id();
        }
        return null;
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
