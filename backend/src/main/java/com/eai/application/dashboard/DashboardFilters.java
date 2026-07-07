package com.eai.application.dashboard;

import java.time.Instant;
import java.util.UUID;

public record DashboardFilters(UUID companyId, UUID storeId, Instant dateFrom, Instant dateTo) {
}
