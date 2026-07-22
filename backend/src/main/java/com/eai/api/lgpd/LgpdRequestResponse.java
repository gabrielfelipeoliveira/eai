package com.eai.api.lgpd;

import com.eai.application.lgpd.LgpdRequestDetails;
import com.eai.domain.lgpd.LgpdRequest;
import com.eai.domain.lgpd.LgpdRequestStatus;
import com.eai.domain.lgpd.LgpdRequestType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LgpdRequestResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        UUID leadId,
        String dataSubjectName,
        String dataSubjectPhone,
        String dataSubjectEmail,
        LgpdRequestType requestType,
        LgpdRequestStatus status,
        String description,
        UUID requestedByUserId,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        List<LgpdRequestActionResponse> actions
) {

    public static LgpdRequestResponse fromDomain(LgpdRequest request) {
        return from(request, List.of());
    }

    public static LgpdRequestResponse fromDetails(LgpdRequestDetails details) {
        return from(details.request(), details.actions().stream()
                .map(LgpdRequestActionResponse::fromDomain)
                .toList());
    }

    private static LgpdRequestResponse from(LgpdRequest request, List<LgpdRequestActionResponse> actions) {
        return new LgpdRequestResponse(
                request.getId(),
                request.getCompanyId(),
                request.getStoreId(),
                request.getLeadId(),
                request.getDataSubjectName(),
                request.getDataSubjectPhone(),
                request.getDataSubjectEmail(),
                request.getRequestType(),
                request.getStatus(),
                request.getDescription(),
                request.getRequestedByUserId(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getCompletedAt(),
                actions
        );
    }
}
