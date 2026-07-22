package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdRequestType;

import java.util.UUID;

public record CreateLgpdRequestCommand(
        UUID companyId,
        UUID storeId,
        UUID leadId,
        String dataSubjectName,
        String dataSubjectPhone,
        String dataSubjectEmail,
        LgpdRequestType requestType,
        String description
) {
}
