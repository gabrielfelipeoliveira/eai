package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdRequestStatus;

import java.util.UUID;

public record LgpdRequestSearchCriteria(
        LgpdRequestStatus status,
        UUID companyId,
        UUID storeId,
        UUID leadId
) {
}
