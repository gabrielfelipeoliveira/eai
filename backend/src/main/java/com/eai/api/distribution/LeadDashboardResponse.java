package com.eai.api.distribution;

import com.eai.application.distribution.LeadDashboardMetrics;

import java.util.List;
import java.util.UUID;

public record LeadDashboardResponse(long unassignedLeads, long overdueLeads, List<LeadsBySellerResponse> leadsBySeller) {

    public static LeadDashboardResponse fromMetrics(LeadDashboardMetrics metrics) {
        return new LeadDashboardResponse(
                metrics.unassignedLeads(),
                metrics.overdueLeads(),
                metrics.leadsBySeller().stream()
                        .map(item -> new LeadsBySellerResponse(item.sellerId(), item.sellerName(), item.leadCount()))
                        .toList()
        );
    }

    public record LeadsBySellerResponse(UUID sellerId, String sellerName, long leadCount) {
    }
}
