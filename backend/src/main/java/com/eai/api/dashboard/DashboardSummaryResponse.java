package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardSummary;

public record DashboardSummaryResponse(
        long totalLeadsToday,
        long totalLeadsThisMonth,
        long availableLeads,
        long assignedLeads,
        long soldLeads,
        long lostLeads,
        double conversionRate,
        double averageFirstResponseTimeMinutes,
        long overdueLeads
) {
    public static DashboardSummaryResponse fromSummary(DashboardSummary summary) {
        return new DashboardSummaryResponse(
                summary.totalLeadsToday(),
                summary.totalLeadsThisMonth(),
                summary.availableLeads(),
                summary.assignedLeads(),
                summary.soldLeads(),
                summary.lostLeads(),
                summary.conversionRate(),
                summary.averageFirstResponseTimeMinutes(),
                summary.overdueLeads()
        );
    }
}
