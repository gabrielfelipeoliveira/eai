import { api } from './api';
import type { AuthUser, UserRole } from '../types/auth';

export interface CreateUserPayload {
  name: string;
  email: string;
  password: string;
  phone?: string;
  jobTitle?: string;
  companyId: string | null;
  storeId: string | null;
  roles: UserRole[];
}

export interface AssignUserTenantPayload {
  companyId: string | null;
  storeId: string | null;
}

export async function listUsers() {
  const response = await api.get<AuthUser[]>('/users');
  return response.data;
}

export async function createUser(payload: CreateUserPayload) {
  const response = await api.post<AuthUser>('/users', payload);
  return response.data;
}

export async function assignUserTenant(userId: string, payload: AssignUserTenantPayload) {
  const response = await api.patch<AuthUser>(`/users/${userId}/tenant`, payload);
  return response.data;
}
