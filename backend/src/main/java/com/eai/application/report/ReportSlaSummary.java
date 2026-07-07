package com.eai.application.report;

public record ReportSlaSummary(
        long leadCount,
        long overdueToAssign,
        long overdueToFirstContact,
        long overdueTotal,
        double averageFirstResponseTimeMinutes,
        long firstContactWithinSla,
        long firstContactOutsideSla
) {
}
