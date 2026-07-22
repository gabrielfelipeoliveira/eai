import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getMe, login as loginRequest, logout as logoutRequest, refreshAuthSession } from '../services/authService';
import { clearTokens, getAccessToken } from '../services/tokenStorage';
import type { AuthUser, UserRole } from '../types/auth';

interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasAnyRole: (roles: UserRole[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadUser = useCallback(async () => {
    try {
      if (!getAccessToken()) {
        await refreshAuthSession();
      }
      setUser(await getMe());
    } catch {
      clearTokens();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadUser();
  }, [loadUser]);

  const login = useCallback(async (email: string, password: string) => {
    await loginRequest(email, password);
    setUser(await getMe());
  }, []);

  const logout = useCallback(async () => {
    await logoutRequest();
    setUser(null);
  }, []);

  const hasAnyRole = useCallback(
    (roles: UserRole[]) => Boolean(user?.roles.some((role) => roles.includes(role))),
    [user],
  );

  const value = useMemo(
    () => ({
      user,
      isLoading,
      isAuthenticated: Boolean(user),
      login,
      logout,
      hasAnyRole,
    }),
    [hasAnyRole, isLoading, login, logout, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
