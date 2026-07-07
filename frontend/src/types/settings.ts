import type { AuthUser } from './auth';
import type { LeadDistributionConfig, LeadDistributionMode } from './distribution';
import type { EmailAccount } from './emailAccount';
import type { MessageTemplate } from './message';
import type { Company, Store, TenantStatus } from './tenant';

export interface SystemPreferences {
  timezone: string;
  locale: string;
  dateFormat: string;
  notificationsEnabled: boolean;
}

export interface Settings {
  company: Company;
  store: Store;
  distribution: LeadDistributionConfig;
  availableCompanies: Company[];
  availableStores: Store[];
  users: AuthUser[];
  templates: MessageTemplate[];
  emailAccounts: EmailAccount[];
  system: SystemPreferences;
}

export interface SettingsCompanyPayload {
  companyId?: string;
  name: string;
  document: string;
  email?: string;
  phone?: string;
  status: TenantStatus;
}

export interface SettingsStorePayload {
  storeId?: string;
  companyId?: string;
  name: string;
  document: string;
  email?: string;
  phone?: string;
  city?: string;
  state?: string;
  address?: string;
  status: TenantStatus;
}

export interface SettingsDistributionPayload {
  companyId?: string;
  storeId?: string;
  mode: LeadDistributionMode;
  active: boolean;
}

export interface SettingsSlaPayload {
  companyId?: string;
  storeId?: string;
  minutesToAssign: number;
  minutesToFirstContact: number;
  active: boolean;
}
