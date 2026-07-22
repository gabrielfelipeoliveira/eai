package com.eai.application.lgpd;

import com.eai.domain.lgpd.LgpdRequest;

import java.util.Optional;
import java.util.UUID;

public interface LgpdRequestRepository {

    LgpdRequestPageResult<LgpdRequest> search(LgpdRequestSearchCriteria criteria, int page, int size);

    Optional<LgpdRequest> findById(UUID id);

    LgpdRequest save(LgpdRequest request);
}
