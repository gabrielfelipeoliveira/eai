package com.eai.api.lgpd;

import com.eai.domain.lgpd.LgpdActionType;
import com.eai.domain.lgpd.LgpdRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LgpdRequestActionRequest(
        @NotNull LgpdActionType actionType,
        @NotBlank String resolution,
        LgpdRequestStatus finalStatus
) {
}
