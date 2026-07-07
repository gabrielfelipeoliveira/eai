package com.eai.api.report;

import com.eai.application.report.ReportLostLeadItem;

import java.time.Instant;
import java.util.UUID;

public record ReportLostLeadResponse(
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
    public static ReportLostLeadResponse fromItem(ReportLostLeadItem item) {
        return new ReportLostLeadResponse(
                item.leadId(),
                item.customerName(),
                item.vehicleInterest(),
                item.sellerId(),
                item.sellerName(),
                item.source(),
                item.lostReason(),
                item.createdAt(),
                item.lostAt()
        );
    }
}
