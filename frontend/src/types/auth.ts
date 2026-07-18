export type UserRole = 'ADMIN' | 'MANAGER' | 'STORE_MANAGER' | 'SELLER' | 'PRE_SALES' | 'F_AND_I' | 'AVALIADOR';

export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  phone: string | null;
  jobTitle: string | null;
  companyId: string;
  storeId: string;
  status: UserStatus;
  roles: UserRole[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
}
