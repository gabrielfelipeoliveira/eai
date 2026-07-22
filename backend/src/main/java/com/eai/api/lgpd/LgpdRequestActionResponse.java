package com.eai.api.lgpd;

import com.eai.domain.lgpd.LgpdActionType;
import com.eai.domain.lgpd.LgpdRequestAction;
import com.eai.domain.lgpd.LgpdRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record LgpdRequestActionResponse(
        UUID id,
        UUID requestId,
        UUID executorUserId,
        LgpdActionType actionType,
        String resolution,
        LgpdRequestStatus finalStatus,
        Instant createdAt
) {

    public static LgpdRequestActionResponse fromDomain(LgpdRequestAction action) {
        return new LgpdRequestActionResponse(
                action.getId(),
                action.getRequestId(),
                action.getExecutorUserId(),
                action.getActionType(),
                action.getResolution(),
                action.getFinalStatus(),
                action.getCreatedAt()
        );
    }
}
