import { api } from './api';
import type { Notification, UnreadNotificationCount } from '../types/notification';

export async function listNotifications(unreadOnly = true, limit = 20) {
  const response = await api.get<Notification[]>('/notifications', {
    params: { unreadOnly, limit },
  });
  return response.data;
}

export async function getUnreadNotificationCount() {
  const response = await api.get<UnreadNotificationCount>('/notifications/unread-count');
  return response.data;
}

export async function markNotificationRead(id: string) {
  const response = await api.post<Notification>(`/notifications/${id}/read`);
  return response.data;
}

export async function markAllNotificationsRead() {
  await api.post('/notifications/read-all');
}
