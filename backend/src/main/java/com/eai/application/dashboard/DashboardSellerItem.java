package com.eai.application.dashboard;

import java.util.UUID;

public record DashboardSellerItem(UUID sellerId, String sellerName, long leadCount, long soldLeads, double conversionRate) {
}
