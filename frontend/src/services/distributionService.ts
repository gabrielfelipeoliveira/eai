import { api } from './api';
import type { LeadDashboardMetrics, LeadDistributionConfig, LeadDistributionConfigPayload } from '../types/distribution';

export async function getDistributionConfig() {
  const response = await api.get<LeadDistributionConfig>('/distribution/config');
  return response.data;
}

export async function updateDistributionConfig(payload: LeadDistributionConfigPayload) {
  const response = await api.put<LeadDistributionConfig>('/distribution/config', payload);
  return response.data;
}

export async function getLeadDashboardMetrics() {
  const response = await api.get<LeadDashboardMetrics>('/dashboard/leads');
  return response.data;
}
