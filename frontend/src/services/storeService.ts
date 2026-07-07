import { api } from './api';
import type { Store, TenantStatus } from '../types/tenant';

export interface StorePayload {
  companyId: string;
  name: string;
  document: string;
  email?: string;
  phone?: string;
  city?: string;
  state?: string;
  address?: string;
  status?: TenantStatus;
}

export interface UpdateStorePayload extends StorePayload {
  status: TenantStatus;
}

export async function listStores(companyId?: string) {
  const response = await api.get<Store[]>('/stores', { params: companyId ? { companyId } : undefined });
  return response.data;
}

export async function createStore(payload: StorePayload) {
  const response = await api.post<Store>('/stores', payload);
  return response.data;
}

export async function updateStore(id: string, payload: UpdateStorePayload) {
  const response = await api.put<Store>(`/stores/${id}`, payload);
  return response.data;
}
