package com.eai.api.lgpd;

import com.eai.domain.lgpd.LgpdRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LgpdRequestCreateRequest(
        @NotNull UUID companyId,
        UUID storeId,
        UUID leadId,
        @NotBlank String dataSubjectName,
        String dataSubjectPhone,
        String dataSubjectEmail,
        @NotNull LgpdRequestType requestType,
        @NotBlank String description
) {
}
