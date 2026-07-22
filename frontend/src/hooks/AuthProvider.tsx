import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { getMe, login as loginRequest, logout as logoutRequest, refreshAuthSession } from '../services/authService';
import { clearTokens, getAccessToken } from '../services/tokenStorage';
import type { AuthUser, UserRole } from '../types/auth';
import { AuthContext } from './AuthContext';

export function AuthProvider({ children }: { children: ReactNode }) {
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
