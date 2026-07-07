import { api } from './api';
import type {
  Settings,
  SettingsCompanyPayload,
  SettingsDistributionPayload,
  SettingsSlaPayload,
  SettingsStorePayload,
} from '../types/settings';
import type { Company, Store } from '../types/tenant';
import type { LeadDistributionConfig } from '../types/distribution';

export async function getSettings(params?: { companyId?: string; storeId?: string }) {
  const response = await api.get<Settings>('/settings', { params });
  return response.data;
}

export async function updateSettingsCompany(payload: SettingsCompanyPayload) {
  const response = await api.put<Company>('/settings/company', payload);
  return response.data;
}

export async function updateSettingsStore(payload: SettingsStorePayload) {
  const response = await api.put<Store>('/settings/store', payload);
  return response.data;
}

export async function updateSettingsDistribution(payload: SettingsDistributionPayload) {
  const response = await api.put<LeadDistributionConfig>('/settings/distribution', payload);
  return response.data;
}

export async function updateSettingsSla(payload: SettingsSlaPayload) {
  const response = await api.put<LeadDistributionConfig>('/settings/sla', payload);
  return response.data;
}
