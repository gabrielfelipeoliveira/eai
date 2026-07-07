package com.eai.application.report;

public record ReportSourceItem(String source, long leadCount, long soldLeads, long lostLeads, double conversionRate) {
}
