import { api } from './api';
import { clearTokens, saveTokens } from './tokenStorage';
import type { AuthTokens, AuthUser } from '../types/auth';

export async function login(email: string, password: string) {
  const response = await api.post<AuthTokens>('/auth/login', { email, password });
  saveTokens(response.data);
  return response.data;
}

export async function logout() {
  try {
    await api.post('/auth/logout');
  } finally {
    clearTokens();
  }
}

export async function getMe() {
  const response = await api.get<AuthUser>('/auth/me');
  return response.data;
}
