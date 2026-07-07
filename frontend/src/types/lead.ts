export type LeadStatus =
  | 'NEW'
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'FIRST_CONTACT'
  | 'IN_NEGOTIATION'
  | 'VISIT_SCHEDULED'
  | 'PROPOSAL_SENT'
  | 'SOLD'
  | 'LOST'
  | 'DUPLICATED';

export type LeadSource =
  | 'MANUAL'
  | 'EMAIL'
  | 'WEBSITE'
  | 'FACEBOOK'
  | 'INSTAGRAM'
  | 'WEBMOTORS'
  | 'ICARROS'
  | 'OLX'
  | 'API';

export interface Lead {
  id: string;
  companyId: string;
  storeId: string;
  customerName: string;
  customerPhone: string | null;
  customerEmail: string | null;
  customerCity: string | null;
  vehicleInterest: string | null;
  source: LeadSource;
  originalMessage: string | null;
  status: LeadStatus;
  assignedToUserId: string | null;
  createdAt: string;
  updatedAt: string;
  firstContactAt: string | null;
  lastContactAt: string | null;
  lostReason: string | null;
  saleValue: number | null;
}

export interface LeadHistory {
  id: string;
  leadId: string;
  userId: string;
  previousStatus: LeadStatus | null;
  newStatus: LeadStatus;
  description: string | null;
  createdAt: string;
}

export interface LeadNote {
  id: string;
  leadId: string;
  userId: string;
  note: string;
  createdAt: string;
}

export interface LeadTag {
  id: string;
  leadId: string;
  name: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
