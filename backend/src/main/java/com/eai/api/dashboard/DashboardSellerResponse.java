package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardSellerItem;

import java.util.UUID;

public record DashboardSellerResponse(UUID sellerId, String sellerName, long leadCount, long soldLeads, double conversionRate) {
    public static DashboardSellerResponse fromItem(DashboardSellerItem item) {
        return new DashboardSellerResponse(item.sellerId(), item.sellerName(), item.leadCount(), item.soldLeads(), item.conversionRate());
    }
}
