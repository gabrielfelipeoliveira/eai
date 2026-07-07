export type LeadDistributionMode = 'MANUAL' | 'ROUND_ROBIN' | 'LEAST_BUSY';

export interface LeadDistributionConfig {
  id: string;
  companyId: string;
  storeId: string;
  mode: LeadDistributionMode;
  active: boolean;
  slaPolicyId: string;
  minutesToAssign: number;
  minutesToFirstContact: number;
  slaActive: boolean;
}

export interface LeadDistributionConfigPayload {
  companyId?: string;
  storeId?: string;
  mode: LeadDistributionMode;
  active: boolean;
  minutesToAssign: number;
  minutesToFirstContact: number;
  slaActive: boolean;
}

export interface LeadDashboardMetrics {
  unassignedLeads: number;
  overdueLeads: number;
  leadsBySeller: Array<{
    sellerId: string;
    sellerName: string;
    leadCount: number;
  }>;
}
