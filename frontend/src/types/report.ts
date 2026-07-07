import type { LeadSource } from './lead';

export interface ReportFilters {
  companyId?: string;
  storeId?: string;
  sellerId?: string;
  source?: LeadSource;
  dateFrom?: string;
  dateTo?: string;
}

export interface ReportLeadPeriodItem {
  period: string;
  leadCount: number;
  soldLeads: number;
  lostLeads: number;
  conversionRate: number;
}

export interface ReportSellerItem {
  sellerId: string;
  sellerName: string;
  leadCount: number;
  soldLeads: number;
  lostLeads: number;
  conversionRate: number;
  averageFirstResponseTimeMinutes: number;
  saleValue: number;
}

export interface ReportSourceItem {
  source: LeadSource;
  leadCount: number;
  soldLeads: number;
  lostLeads: number;
  conversionRate: number;
}

export interface ReportLostLeadItem {
  leadId: string;
  customerName: string;
  vehicleInterest: string | null;
  sellerId: string | null;
  sellerName: string;
  source: LeadSource;
  lostReason: string | null;
  createdAt: string;
  lostAt: string;
}

export interface ReportSaleItem {
  leadId: string;
  customerName: string;
  vehicleInterest: string | null;
  sellerId: string | null;
  sellerName: string;
  source: LeadSource;
  saleValue: number | null;
  createdAt: string;
  soldAt: string;
}

export interface ReportSlaSummary {
  leadCount: number;
  overdueToAssign: number;
  overdueToFirstContact: number;
  overdueTotal: number;
  averageFirstResponseTimeMinutes: number;
  firstContactWithinSla: number;
  firstContactOutsideSla: number;
}
