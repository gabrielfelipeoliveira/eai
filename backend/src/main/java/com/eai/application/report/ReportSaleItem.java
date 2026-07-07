package com.eai.application.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReportSaleItem(
        UUID leadId,
        String customerName,
        String vehicleInterest,
        UUID sellerId,
        String sellerName,
        String source,
        BigDecimal saleValue,
        Instant createdAt,
        Instant soldAt
) {
}
