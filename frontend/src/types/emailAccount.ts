export type EmailProtocol = 'IMAP';

export type EmailAccountStatus = 'NEVER_SYNCED' | 'SUCCESS' | 'FAILED';

export interface EmailAccount {
  id: string;
  companyId: string;
  storeId: string;
  name: string;
  host: string;
  port: number;
  username: string;
  protocol: EmailProtocol;
  useSsl: boolean;
  active: boolean;
  lastReadAt: string | null;
  createdAt: string;
  updatedAt: string;
  lastSyncStatus: EmailAccountStatus;
  lastSyncMessage: string | null;
  lastSyncAt: string | null;
}

export interface EmailImportResult {
  messagesRead: number;
  leadsCreated: number;
  duplicatesMarked: number;
  status: 'SUCCESS' | 'FAILED';
  message: string;
}
