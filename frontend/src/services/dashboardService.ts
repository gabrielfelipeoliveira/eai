import { api } from './api';
import type {
  DashboardChartItem,
  DashboardFilters,
  DashboardSalesPeriodItem,
  DashboardSellerItem,
  DashboardSummary,
} from '../types/dashboard';

function cleanParams(filters: DashboardFilters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== undefined && value !== ''));
}

export async function getDashboardSummary(filters: DashboardFilters) {
  const response = await api.get<DashboardSummary>('/dashboard/summary', { params: cleanParams(filters) });
  return response.data;
}

export async function getLeadsBySource(filters: DashboardFilters) {
  const response = await api.get<DashboardChartItem[]>('/dashboard/leads-by-source', { params: cleanParams(filters) });
  return response.data;
}

export async function getLeadsByStatus(filters: DashboardFilters) {
  const response = await api.get<DashboardChartItem[]>('/dashboard/leads-by-status', { params: cleanParams(filters) });
  return response.data;
}

export async function getLeadsBySeller(filters: DashboardFilters) {
  const response = await api.get<DashboardSellerItem[]>('/dashboard/leads-by-seller', { params: cleanParams(filters) });
  return response.data;
}

export async function getSalesByPeriod(filters: DashboardFilters) {
  const response = await api.get<DashboardSalesPeriodItem[]>('/dashboard/sales-by-period', { params: cleanParams(filters) });
  return response.data;
}
