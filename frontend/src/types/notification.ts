export type NotificationType = 'EMAIL_ACCOUNT_FAILURE';

export type NotificationSeverity = 'INFO' | 'WARNING' | 'ERROR';

export type NotificationExternalDeliveryStatus = 'PENDING_EXTERNAL_DELIVERY' | 'DELIVERED' | 'FAILED';

export interface Notification {
  id: string;
  recipientUserId: string;
  type: NotificationType;
  severity: NotificationSeverity;
  title: string;
  message: string;
  relatedEntityType: string | null;
  relatedEntityId: string | null;
  externalDeliveryStatus: NotificationExternalDeliveryStatus;
  read: boolean;
  readAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UnreadNotificationCount {
  count: number;
}
