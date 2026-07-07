package com.eai.application.report;

import java.util.List;

public interface ReportExporter {

    boolean supports(ReportExportFormat format);

    ExportedReport exportLeadPeriods(List<ReportLeadPeriodItem> items);

    ExportedReport exportSellers(List<ReportSellerItem> items);
}
