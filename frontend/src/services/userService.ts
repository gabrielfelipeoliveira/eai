import { api } from './api';
import type { AuthUser, UserRole } from '../types/auth';

export interface CreateUserPayload {
  name: string;
  email: string;
  password: string;
  phone?: string;
  jobTitle?: string;
  roles: UserRole[];
}

export async function listUsers() {
  const response = await api.get<AuthUser[]>('/users');
  return response.data;
}

export async function createUser(payload: CreateUserPayload) {
  const response = await api.post<AuthUser>('/users', payload);
  return response.data;
}
