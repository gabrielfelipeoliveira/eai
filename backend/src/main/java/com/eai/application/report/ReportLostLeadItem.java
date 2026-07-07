package com.eai.application.report;

import java.time.Instant;
import java.util.UUID;

public record ReportLostLeadItem(
        UUID leadId,
        String customerName,
        String vehicleInterest,
        UUID sellerId,
        String sellerName,
        String source,
        String lostReason,
        Instant createdAt,
        Instant lostAt
) {
}
