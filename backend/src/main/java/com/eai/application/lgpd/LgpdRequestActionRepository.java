package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdRequestAction;

import java.util.List;
import java.util.UUID;

public interface LgpdRequestActionRepository {

    List<LgpdRequestAction> findByRequestId(UUID requestId);

    LgpdRequestAction save(LgpdRequestAction action);
}
