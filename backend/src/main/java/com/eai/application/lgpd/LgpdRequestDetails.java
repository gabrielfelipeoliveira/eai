package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdRequest;
import com.eai.domain.lgpd.LgpdRequestAction;

import java.util.List;

public record LgpdRequestDetails(
        LgpdRequest request,
        List<LgpdRequestAction> actions
) {
}
