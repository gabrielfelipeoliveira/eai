import { createContext } from 'react';
import type { AuthUser, UserRole } from '../types/auth';

export interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasAnyRole: (roles: UserRole[]) => boolean;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
