export type UserRole = 'ADMIN' | 'MANAGER' | 'SELLER' | 'RECEPTIONIST' | 'AUDITOR';

export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  phone: string | null;
  jobTitle: string | null;
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
