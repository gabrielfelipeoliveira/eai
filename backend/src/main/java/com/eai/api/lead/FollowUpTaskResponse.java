package com.eai.api.lead;

import com.eai.domain.lead.FollowUpTask;
import com.eai.domain.lead.FollowUpTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record FollowUpTaskResponse(
        UUID id,
        UUID leadId,
        UUID userId,
        String title,
        String description,
        Instant dueAt,
        Instant completedAt,
        FollowUpTaskStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static FollowUpTaskResponse fromDomain(FollowUpTask task) {
        return new FollowUpTaskResponse(
                task.getId(),
                task.getLeadId(),
                task.getUserId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueAt(),
                task.getCompletedAt(),
                task.effectiveStatus(Instant.now()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
