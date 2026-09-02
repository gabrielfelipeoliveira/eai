import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MainLayout } from './MainLayout';
import type { AuthUser, UserRole } from '../types/auth';

const authState: {
  logout: ReturnType<typeof vi.fn>;
  roles: UserRole[];
  user: AuthUser;
} = {
  logout: vi.fn(),
  roles: ['ADMIN'],
  user: {
    id: 'user-1',
    name: 'Lucas Reiter',
    email: 'lucas@example.com',
    phone: null,
    jobTitle: null,
    companyId: 'company-1',
    storeId: 'store-1',
    status: 'ACTIVE',
    roles: ['ADMIN'],
    createdAt: '2026-09-02T00:00:00Z',
    updatedAt: '2026-09-02T00:00:00Z',
  },
};

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    hasAnyRole: (roles: UserRole[]) => roles.some((role) => authState.roles.includes(role)),
    logout: authState.logout,
    user: { ...authState.user, roles: authState.roles },
  }),
}));

vi.mock('../hooks/useMetadata', () => ({
  useMetadata: () => ({
    label: (_collection: string, code: string | null | undefined) => code ?? '-',
  }),
}));

vi.mock('../services/notificationService', () => ({
  getUnreadNotificationCount: vi.fn().mockResolvedValue({ count: 0 }),
  listNotifications: vi.fn().mockResolvedValue([]),
  markAllNotificationsRead: vi.fn().mockResolvedValue(undefined),
  markNotificationRead: vi.fn().mockResolvedValue(undefined),
}));

function renderLayout(roles: UserRole[] = ['ADMIN']) {
  authState.roles = roles;
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<MainLayout />} path="/">
            <Route element={<h2>Conteudo da rota</h2>} index />
          </Route>
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('MainLayout', () => {
  beforeEach(() => {
    authState.logout.mockReset();
    authState.roles = ['ADMIN'];
  });

  it('deve renderizar shell, botao mobile de menu e conteudo da rota', async () => {
    const user = userEvent.setup();
    renderLayout();

    expect(screen.getByRole('heading', { name: 'Operacao comercial' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Abrir menu' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Conteudo da rota' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Abrir menu' }));

    expect(screen.getAllByRole('link', { name: /Dashboard/ }).length).toBeGreaterThan(0);
  });

  it('deve esconder atalhos administrativos para usuario vendedor', () => {
    renderLayout(['SELLER']);

    expect(screen.getAllByRole('link', { name: /Leads/ }).length).toBeGreaterThan(0);
    expect(screen.getAllByRole('link', { name: /Conversas/ }).length).toBeGreaterThan(0);
    expect(screen.queryByRole('link', { name: /Usuarios/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Empresas/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Notificacoes' })).not.toBeInTheDocument();
  });
});
