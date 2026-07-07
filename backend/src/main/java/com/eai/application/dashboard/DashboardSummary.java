package com.eai.application.dashboard;

public record DashboardSummary(
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
}
