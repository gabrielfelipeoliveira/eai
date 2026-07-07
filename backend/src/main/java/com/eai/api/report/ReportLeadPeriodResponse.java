package com.eai.api.report;

import com.eai.application.report.ReportLeadPeriodItem;

public record ReportLeadPeriodResponse(String period, long leadCount, long soldLeads, long lostLeads, double conversionRate) {
    public static ReportLeadPeriodResponse fromItem(ReportLeadPeriodItem item) {
        return new ReportLeadPeriodResponse(item.period(), item.leadCount(), item.soldLeads(), item.lostLeads(), item.conversionRate());
    }
}
