package com.eai.api.report;

import com.eai.application.report.ReportSellerItem;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportSellerResponse(
        UUID sellerId,
        String sellerName,
        long leadCount,
        long soldLeads,
        long lostLeads,
        double conversionRate,
        double averageFirstResponseTimeMinutes,
        BigDecimal saleValue
) {
    public static ReportSellerResponse fromItem(ReportSellerItem item) {
        return new ReportSellerResponse(
                item.sellerId(),
                item.sellerName(),
                item.leadCount(),
                item.soldLeads(),
                item.lostLeads(),
                item.conversionRate(),
                item.averageFirstResponseTimeMinutes(),
                item.saleValue()
        );
    }
}
