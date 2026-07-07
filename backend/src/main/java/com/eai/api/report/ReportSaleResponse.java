package com.eai.api.report;

import com.eai.application.report.ReportSaleItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReportSaleResponse(
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
    public static ReportSaleResponse fromItem(ReportSaleItem item) {
        return new ReportSaleResponse(
                item.leadId(),
                item.customerName(),
                item.vehicleInterest(),
                item.sellerId(),
                item.sellerName(),
                item.source(),
                item.saleValue(),
                item.createdAt(),
                item.soldAt()
        );
    }
}
