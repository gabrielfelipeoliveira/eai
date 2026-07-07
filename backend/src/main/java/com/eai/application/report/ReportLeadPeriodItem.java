package com.eai.application.report;

public record ReportLeadPeriodItem(String period, long leadCount, long soldLeads, long lostLeads, double conversionRate) {
}
