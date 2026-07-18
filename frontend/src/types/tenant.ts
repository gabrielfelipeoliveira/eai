export type TenantStatus = 'ACTIVE' | 'INACTIVE';

export interface Company {
  id: string;
  name: string;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Store {
  id: string;
  companyId: string;
  name: string;
  document: string;
  email: string | null;
  phone: string | null;
  city: string | null;
  state: string | null;
  address: string | null;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
}
