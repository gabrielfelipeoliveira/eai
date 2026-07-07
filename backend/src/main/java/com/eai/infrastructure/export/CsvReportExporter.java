package com.eai.infrastructure.export;

import com.eai.application.report.ExportedReport;
import com.eai.application.report.ReportExportFormat;
import com.eai.application.report.ReportExporter;
import com.eai.application.report.ReportLeadPeriodItem;
import com.eai.application.report.ReportSellerItem;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CsvReportExporter implements ReportExporter {

    private static final String CONTENT_TYPE = "text/csv; charset=UTF-8";

    @Override
    public boolean supports(ReportExportFormat format) {
        return format == ReportExportFormat.CSV;
    }

    @Override
    public ExportedReport exportLeadPeriods(List<ReportLeadPeriodItem> items) {
        Stream<String> rows = items.stream()
                .map(item -> row(
                        item.period(),
                        item.leadCount(),
                        item.soldLeads(),
                        item.lostLeads(),
                        item.conversionRate()
                ));
        return csv("reports-leads.csv", "period,leadCount,soldLeads,lostLeads,conversionRate", rows);
    }

    @Override
    public ExportedReport exportSellers(List<ReportSellerItem> items) {
        Stream<String> rows = items.stream()
                .map(item -> row(
                        item.sellerId(),
                        item.sellerName(),
                        item.leadCount(),
                        item.soldLeads(),
                        item.lostLeads(),
                        item.conversionRate(),
                        item.averageFirstResponseTimeMinutes(),
                        item.saleValue()
                ));
        return csv("reports-sellers.csv", "sellerId,sellerName,leadCount,soldLeads,lostLeads,conversionRate,averageFirstResponseTimeMinutes,saleValue", rows);
    }

    private ExportedReport csv(String filename, String header, Stream<String> rows) {
        String content = Stream.concat(Stream.of(header), rows).collect(Collectors.joining("\n")) + "\n";
        return new ExportedReport(filename, CONTENT_TYPE, content.getBytes(StandardCharsets.UTF_8));
    }

    private String row(Object... values) {
        return Stream.of(values)
                .map(this::cell)
                .collect(Collectors.joining(","));
    }

    private String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
