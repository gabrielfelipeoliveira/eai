package com.eai.application.lead;

import com.eai.domain.lead.FollowUpTask;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpTaskRepository {

    FollowUpTask save(FollowUpTask task);

    Optional<FollowUpTask> findById(UUID id);

    List<FollowUpTask> findVisible(UUID scopeCompanyId, UUID scopeStoreId, UUID userId);

    List<FollowUpTask> findByLeadId(UUID leadId);
}
