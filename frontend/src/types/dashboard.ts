export interface DashboardFilters {
  companyId?: string;
  storeId?: string;
  dateFrom?: string;
  dateTo?: string;
}

export interface DashboardSummary {
  totalLeadsToday: number;
  totalLeadsThisMonth: number;
  availableLeads: number;
  assignedLeads: number;
  soldLeads: number;
  lostLeads: number;
  conversionRate: number;
  averageFirstResponseTimeMinutes: number;
  overdueLeads: number;
}

export interface DashboardChartItem {
  label: string;
  value: number;
}

export interface DashboardSellerItem {
  sellerId: string;
  sellerName: string;
  leadCount: number;
  soldLeads: number;
  conversionRate: number;
}

export interface DashboardSalesPeriodItem {
  period: string;
  soldLeads: number;
  saleValue: number;
}
