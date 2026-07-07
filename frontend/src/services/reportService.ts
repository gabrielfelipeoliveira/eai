import { api } from './api';
import type {
  ReportFilters,
  ReportLeadPeriodItem,
  ReportLostLeadItem,
  ReportSaleItem,
  ReportSellerItem,
  ReportSlaSummary,
  ReportSourceItem,
} from '../types/report';

function cleanParams(filters: ReportFilters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== undefined && value !== ''));
}

export async function getReportLeads(filters: ReportFilters) {
  const response = await api.get<ReportLeadPeriodItem[]>('/reports/leads', { params: cleanParams(filters) });
  return response.data;
}

export async function getReportSellers(filters: ReportFilters) {
  const response = await api.get<ReportSellerItem[]>('/reports/sellers', { params: cleanParams(filters) });
  return response.data;
}

export async function getReportSources(filters: ReportFilters) {
  const response = await api.get<ReportSourceItem[]>('/reports/sources', { params: cleanParams(filters) });
  return response.data;
}

export async function getReportLost(filters: ReportFilters) {
  const response = await api.get<ReportLostLeadItem[]>('/reports/lost', { params: cleanParams(filters) });
  return response.data;
}

export async function getReportSales(filters: ReportFilters) {
  const response = await api.get<ReportSaleItem[]>('/reports/sales', { params: cleanParams(filters) });
  return response.data;
}

export async function getReportSla(filters: ReportFilters) {
  const response = await api.get<ReportSlaSummary>('/reports/sla', { params: cleanParams(filters) });
  return response.data;
}

export async function downloadReportCsv(path: '/reports/leads/export.csv' | '/reports/sellers/export.csv', filters: ReportFilters, filename: string) {
  const response = await api.get<Blob>(path, {
    params: cleanParams(filters),
    responseType: 'blob',
  });
  const url = URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
