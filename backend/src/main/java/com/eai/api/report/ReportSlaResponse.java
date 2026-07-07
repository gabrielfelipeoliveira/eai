package com.eai.api.report;

import com.eai.application.report.ReportSlaSummary;

public record ReportSlaResponse(
        long leadCount,
        long overdueToAssign,
        long overdueToFirstContact,
        long overdueTotal,
        double averageFirstResponseTimeMinutes,
        long firstContactWithinSla,
        long firstContactOutsideSla
) {
    public static ReportSlaResponse fromSummary(ReportSlaSummary summary) {
        return new ReportSlaResponse(
                summary.leadCount(),
                summary.overdueToAssign(),
                summary.overdueToFirstContact(),
                summary.overdueTotal(),
                summary.averageFirstResponseTimeMinutes(),
                summary.firstContactWithinSla(),
                summary.firstContactOutsideSla()
        );
    }
}
