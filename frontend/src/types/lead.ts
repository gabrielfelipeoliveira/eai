export type LeadStatus =
  | 'NEW'
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'FIRST_CONTACT'
  | 'IN_NEGOTIATION'
  | 'VISIT_SCHEDULED'
  | 'SIMULATING'
  | 'PROPOSAL_APPROVED'
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

export type FollowUpTaskStatus = 'PENDING' | 'DONE' | 'CANCELED' | 'OVERDUE';

export interface Lead {
  id: string;
  companyId: string;
  storeId: string;
  customerName: string;
  customerPhone: string | null;
  additionalPhones: string[];
  customerEmail: string | null;
  customerCity: string | null;
  vehicleInterest: string | null;
  itemId: string | null;
  item: LeadItem | null;
  source: LeadSource;
  originalMessage: string | null;
  status: LeadStatus;
  assignedToUserId: string | null;
  assignedAt: string | null;
  createdAt: string;
  updatedAt: string;
  firstContactAt: string | null;
  lastContactAt: string | null;
  lostReason: string | null;
  saleValue: number | null;
  saleCurrency: string;
  relatedLeadId: string | null;
  overdueToAssign: boolean;
  overdueToFirstContact: boolean;
}

export interface LeadItem {
  id: string;
  ownerUserId: string;
  name: string | null;
  vehicle: LeadVehicle | null;
  createdAt: string;
  updatedAt: string;
}

export interface LeadVehicle {
  id: string;
  itemId: string;
  name: string | null;
  year: number | null;
  model: string | null;
  value: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface FollowUpTask {
  id: string;
  leadId: string;
  userId: string;
  title: string;
  description: string | null;
  dueAt: string;
  completedAt: string | null;
  status: FollowUpTaskStatus;
  createdAt: string;
  updatedAt: string;
}

export type PipelineResponse = Record<LeadStatus, Lead[]>;

export interface LeadHistory {
  id: string;
  leadId: string;
  userId: string | null;
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
