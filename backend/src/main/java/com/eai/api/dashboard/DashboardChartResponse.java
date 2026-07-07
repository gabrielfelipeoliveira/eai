package com.eai.api.dashboard;

import com.eai.application.dashboard.DashboardChartItem;

public record DashboardChartResponse(String label, long value) {
    public static DashboardChartResponse fromItem(DashboardChartItem item) {
        return new DashboardChartResponse(item.label(), item.value());
    }
}
