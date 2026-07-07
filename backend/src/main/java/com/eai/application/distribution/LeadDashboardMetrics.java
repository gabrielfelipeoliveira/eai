package com.eai.application.distribution;

import java.util.List;
import java.util.UUID;

public record LeadDashboardMetrics(long unassignedLeads, long overdueLeads, List<LeadsBySeller> leadsBySeller) {

    public record LeadsBySeller(UUID sellerId, String sellerName, long leadCount) {
    }
}
