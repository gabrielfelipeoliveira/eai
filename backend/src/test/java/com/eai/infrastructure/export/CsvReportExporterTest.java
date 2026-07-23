package com.eai.infrastructure.export;

import com.eai.application.report.ExportedReport;
import com.eai.application.report.ReportExportFormat;
import com.eai.application.report.ReportLeadPeriodItem;
import com.eai.application.report.ReportSellerItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReportExporterTest {

    private final CsvReportExporter exporter = new CsvReportExporter();

    @DisplayName("Exportador suporta apenas formato CSV")
    @Test
    void supportsOnlyCsvFormat() {
        assertThat(exporter.supports(ReportExportFormat.CSV)).isTrue();
    }

    @DisplayName("Exportacao de leads por periodo inclui cabecalho e linhas")
    @Test
    void exportLeadPeriodsIncludesHeaderAndRows() {
        ExportedReport report = exporter.exportLeadPeriods(List.of(
                new ReportLeadPeriodItem("2026-01-01", 3, 2, 1, 66.67)
        ));

        assertThat(report.filename()).isEqualTo("reports-leads.csv");
        assertThat(report.contentType()).isEqualTo("text/csv; charset=UTF-8");
        assertThat(content(report)).isEqualTo("""
                period,leadCount,soldLeads,lostLeads,conversionRate
                2026-01-01,3,2,1,66.67
                """);
    }

    @DisplayName("Exportacao de vendedores escapa virgulas aspas e quebras de linha")
    @Test
    void exportSellersEscapesCsvCells() {
        ExportedReport report = exporter.exportSellers(List.of(
                new ReportSellerItem(
                        UUID.fromString("00000000-0000-0000-0000-000000000301"),
                        "Ana, \"Vendas\"\nSul",
                        4,
                        2,
                        1,
                        66.67,
                        12.5,
                        BigDecimal.valueOf(120000)
                )
        ));

        assertThat(content(report)).isEqualTo("""
                sellerId,sellerName,leadCount,soldLeads,lostLeads,conversionRate,averageFirstResponseTimeMinutes,saleValue
                00000000-0000-0000-0000-000000000301,"Ana, ""Vendas""
                Sul",4,2,1,66.67,12.5,120000
                """);
    }

    private String content(ExportedReport report) {
        return new String(report.content(), StandardCharsets.UTF_8);
    }
}
