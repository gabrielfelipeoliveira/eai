package com.eai.application.lead;

import com.eai.domain.lead.Lead;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository {

    PageResult<Lead> search(LeadSearchCriteria criteria, int page, int size);

    Optional<Lead> findById(UUID id);

    Lead save(Lead lead);

    boolean existsByStoreIdAndPhoneAndVehicleSince(UUID storeId, String phone, String vehicleInterest, Instant since);
}
