package com.eai.application.dashboard;

import java.math.BigDecimal;

public record DashboardSalesPeriodItem(String period, long soldLeads, BigDecimal saleValue) {
}
