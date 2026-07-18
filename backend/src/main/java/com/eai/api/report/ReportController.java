package com.eai.api.report;

import com.eai.application.report.ExportedReport;
import com.eai.application.report.ReportFilters;
import com.eai.application.report.ReportService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.lead.LeadSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/leads")
    public List<ReportLeadPeriodResponse> leads(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return reportService.leadsByPeriod(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser).stream()
                .map(ReportLeadPeriodResponse::fromItem)
                .toList();
    }

    @GetMapping("/sellers")
    public List<ReportSellerResponse> sellers(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return reportService.leadsBySeller(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser).stream()
                .map(ReportSellerResponse::fromItem)
                .toList();
    }

    @GetMapping("/sources")
    public List<ReportSourceResponse> sources(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return reportService.leadsBySource(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser).stream()
                .map(ReportSourceResponse::fromItem)
                .toList();
    }

    @GetMapping("/lost")
    public List<ReportLostLeadResponse> lost(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return reportService.lostLeads(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser).stream()
                .map(ReportLostLeadResponse::fromItem)
                .toList();
    }

    @GetMapping("/sales")
    public List<ReportSaleResponse> sales(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return reportService.sales(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser).stream()
                .map(ReportSaleResponse::fromItem)
                .toList();
    }

    @GetMapping("/sla")
    public ReportSlaResponse sla(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ReportSlaResponse.fromSummary(reportService.sla(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser));
    }

    @GetMapping("/leads/export.csv")
    public ResponseEntity<byte[]> exportLeadsCsv(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return toDownload(reportService.exportLeadPeriodsCsv(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser));
    }

    @GetMapping("/sellers/export.csv")
    public ResponseEntity<byte[]> exportSellersCsv(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return toDownload(reportService.exportSellersCsv(toFilters(companyId, storeId, sellerId, source, dateFrom, dateTo), authenticatedUser));
    }

    private ResponseEntity<byte[]> toDownload(ExportedReport report) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(report.filename()).build().toString())
                .body(report.content());
    }

    private ReportFilters toFilters(UUID companyId, UUID storeId, UUID sellerId, LeadSource source, LocalDate dateFrom, LocalDate dateTo) {
        Instant from = dateFrom == null ? null : dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = dateTo == null ? null : dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new ReportFilters(companyId, storeId, sellerId, source, from, to);
    }
}
