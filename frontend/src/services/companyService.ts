import { api } from './api';
import type { Company, TenantStatus } from '../types/tenant';

export interface CompanyPayload {
  name: string;
  status?: TenantStatus;
}

export interface UpdateCompanyPayload extends CompanyPayload {
  status: TenantStatus;
}

export async function listCompanies() {
  const response = await api.get<Company[]>('/companies');
  return response.data;
}

export async function createCompany(payload: CompanyPayload) {
  const response = await api.post<Company>('/companies', payload);
  return response.data;
}

export async function updateCompany(id: string, payload: UpdateCompanyPayload) {
  const response = await api.put<Company>(`/companies/${id}`, payload);
  return response.data;
}
