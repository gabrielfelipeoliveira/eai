package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdActionType;
import com.eai.domain.lgpd.LgpdRequestStatus;

public record RegisterLgpdRequestActionCommand(
        LgpdActionType actionType,
        String resolution,
        LgpdRequestStatus finalStatus
) {
}
