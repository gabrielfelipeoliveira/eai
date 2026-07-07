package com.eai.application.report;

import com.eai.domain.lead.LeadSource;

import java.time.Instant;
import java.util.UUID;

public record ReportFilters(
        UUID companyId,
        UUID storeId,
        UUID sellerId,
        LeadSource source,
        Instant dateFrom,
        Instant dateTo
) {
}
