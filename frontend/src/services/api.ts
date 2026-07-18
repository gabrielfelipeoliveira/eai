import axios from 'axios';
import { clearTokens, getAccessToken, getRefreshToken, saveTokens } from './tokenStorage';
import type { AuthTokens } from '../types/auth';

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

export function apiErrorCode(error: unknown) {
  return (error as { response?: { data?: { code?: string } } }).response?.data?.code;
}

export function apiErrorMessage(error: unknown) {
  const message = (error as { response?: { data?: { message?: string } } }).response?.data?.message;
  return message && message.trim() ? message : undefined;
}

export const api = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const publicApi = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  const isAuthEndpoint = config.url?.startsWith('/auth/login') || config.url?.startsWith('/auth/refresh');
  if (token && !isAuthEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const refreshToken = getRefreshToken();
    const isAuthEndpoint =
      originalRequest?.url?.startsWith('/auth/login') || originalRequest?.url?.startsWith('/auth/refresh');

    if (error.response?.status !== 401 || !refreshToken || originalRequest?._retry || isAuthEndpoint) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const response = await publicApi.post<AuthTokens>('/auth/refresh', { refreshToken });
      saveTokens(response.data);
      originalRequest.headers.Authorization = `Bearer ${response.data.accessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      if ((refreshError as { response?: { status?: number } }).response?.status === 401) {
        clearTokens();
      }
      return Promise.reject(refreshError);
    }
  },
);
