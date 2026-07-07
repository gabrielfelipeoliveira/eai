package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardSalesPeriodItem;

import java.math.BigDecimal;

public record DashboardSalesPeriodResponse(String period, long soldLeads, BigDecimal saleValue) {
    public static DashboardSalesPeriodResponse fromItem(DashboardSalesPeriodItem item) {
        return new DashboardSalesPeriodResponse(item.period(), item.soldLeads(), item.saleValue());
    }
}
