package com.eai.api.report;

import com.eai.application.report.ReportSourceItem;

public record ReportSourceResponse(String source, long leadCount, long soldLeads, long lostLeads, double conversionRate) {
    public static ReportSourceResponse fromItem(ReportSourceItem item) {
        return new ReportSourceResponse(item.source(), item.leadCount(), item.soldLeads(), item.lostLeads(), item.conversionRate());
    }
}
