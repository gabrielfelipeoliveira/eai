package com.eai.application.report;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportSellerItem(
        UUID sellerId,
        String sellerName,
        long leadCount,
        long soldLeads,
        long lostLeads,
        double conversionRate,
        double averageFirstResponseTimeMinutes,
        BigDecimal saleValue
) {
}
