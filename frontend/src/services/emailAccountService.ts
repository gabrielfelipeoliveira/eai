import { api } from './api';
import type { EmailAccount, EmailImportResult, EmailProtocol } from '../types/emailAccount';

export interface EmailAccountPayload {
  companyId: string;
  storeId: string;
  name: string;
  host: string;
  port: number;
  username: string;
  password?: string;
  protocol: EmailProtocol;
  useSsl: boolean;
  active: boolean;
}

export async function listEmailAccounts() {
  const response = await api.get<EmailAccount[]>('/email-accounts');
  return response.data;
}

export async function createEmailAccount(payload: EmailAccountPayload) {
  const response = await api.post<EmailAccount>('/email-accounts', payload);
  return response.data;
}

export async function updateEmailAccount(id: string, payload: EmailAccountPayload) {
  const response = await api.put<EmailAccount>(`/email-accounts/${id}`, payload);
  return response.data;
}

export async function deleteEmailAccount(id: string) {
  await api.delete(`/email-accounts/${id}`);
}

export async function testEmailAccount(id: string) {
  const response = await api.post<EmailImportResult>(`/email-accounts/${id}/test`);
  return response.data;
}

export async function syncEmailAccount(id: string) {
  const response = await api.post<EmailImportResult>(`/email-accounts/${id}/sync`);
  return response.data;
}
