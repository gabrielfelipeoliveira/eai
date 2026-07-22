package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardAnalyticsService;
import com.eai.application.dashboard.DashboardFilters;
import com.eai.application.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SELLER')")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return DashboardSummaryResponse.fromSummary(analyticsService.summary(toFilters(companyId, storeId, dateFrom, dateTo), authenticatedUser));
    }

    @GetMapping("/leads-by-source")
    public List<DashboardChartResponse> leadsBySource(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return analyticsService.leadsBySource(toFilters(companyId, storeId, dateFrom, dateTo), authenticatedUser).stream()
                .map(DashboardChartResponse::fromItem)
                .toList();
    }

    @GetMapping("/leads-by-status")
    public List<DashboardChartResponse> leadsByStatus(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return analyticsService.leadsByStatus(toFilters(companyId, storeId, dateFrom, dateTo), authenticatedUser).stream()
                .map(DashboardChartResponse::fromItem)
                .toList();
    }

    @GetMapping("/leads-by-seller")
    public List<DashboardSellerResponse> leadsBySeller(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return analyticsService.leadsBySeller(toFilters(companyId, storeId, dateFrom, dateTo), authenticatedUser).stream()
                .map(DashboardSellerResponse::fromItem)
                .toList();
    }

    @GetMapping("/sales-by-period")
    public List<DashboardSalesPeriodResponse> salesByPeriod(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return analyticsService.salesByPeriod(toFilters(companyId, storeId, dateFrom, dateTo), authenticatedUser).stream()
                .map(DashboardSalesPeriodResponse::fromItem)
                .toList();
    }

    private DashboardFilters toFilters(UUID companyId, UUID storeId, LocalDate dateFrom, LocalDate dateTo) {
        Instant from = dateFrom == null ? null : dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = dateTo == null ? null : dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new DashboardFilters(companyId, storeId, from, to);
    }
}
