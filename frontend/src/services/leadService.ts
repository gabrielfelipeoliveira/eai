import { api } from './api';
import type { Lead, LeadHistory, LeadNote, LeadSource, LeadStatus, LeadTag, PageResponse } from '../types/lead';

export interface LeadPayload {
  companyId: string;
  storeId: string;
  customerName: string;
  customerPhone?: string;
  customerEmail?: string;
  customerCity?: string;
  vehicleInterest?: string;
  source: LeadSource;
  originalMessage?: string;
  status?: LeadStatus;
  assignedToUserId?: string;
  firstContactAt?: string;
  lastContactAt?: string;
  lostReason?: string;
  saleValue?: number;
}

export interface LeadFilters {
  status?: LeadStatus;
  source?: LeadSource;
  assignedToUserId?: string;
  storeId?: string;
  createdFrom?: string;
  createdTo?: string;
  text?: string;
  vehicle?: string;
  phone?: string;
  page?: number;
  size?: number;
}

function cleanParams(filters: LeadFilters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== undefined && value !== ''));
}

export async function listLeads(filters: LeadFilters) {
  const response = await api.get<PageResponse<Lead>>('/leads', { params: cleanParams(filters) });
  return response.data;
}

export async function getLead(id: string) {
  const response = await api.get<Lead>(`/leads/${id}`);
  return response.data;
}

export async function createLead(payload: LeadPayload) {
  const response = await api.post<Lead>('/leads', payload);
  return response.data;
}

export async function updateLead(id: string, payload: LeadPayload) {
  const response = await api.put<Lead>(`/leads/${id}`, payload);
  return response.data;
}

export async function changeLeadStatus(id: string, status: LeadStatus, description?: string) {
  const response = await api.patch<Lead>(`/leads/${id}/status`, { status, description });
  return response.data;
}

export async function assignLeadToMe(id: string) {
  const response = await api.patch<Lead>(`/leads/${id}/assign-to-me`);
  return response.data;
}

export async function assignLead(id: string, userId: string) {
  const response = await api.patch<Lead>(`/leads/${id}/assign/${userId}`);
  return response.data;
}

export async function addLeadNote(id: string, note: string) {
  const response = await api.post<LeadNote>(`/leads/${id}/notes`, { note });
  return response.data;
}

export async function listLeadHistory(id: string) {
  const response = await api.get<LeadHistory[]>(`/leads/${id}/history`);
  return response.data;
}

export async function listLeadNotes(id: string) {
  const response = await api.get<LeadNote[]>(`/leads/${id}/notes`);
  return response.data;
}

export async function listLeadTags(id: string) {
  const response = await api.get<LeadTag[]>(`/leads/${id}/tags`);
  return response.data;
}

export async function addLeadTag(id: string, name: string) {
  const response = await api.post<LeadTag>(`/leads/${id}/tags`, { name });
  return response.data;
}

export async function deleteLeadTag(id: string, tagId: string) {
  await api.delete(`/leads/${id}/tags/${tagId}`);
}
