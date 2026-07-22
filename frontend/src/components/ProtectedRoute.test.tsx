import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';
import type { UserRole } from '../types/auth';

const authState: {
  hasAnyRole: ReturnType<typeof vi.fn>;
  isAuthenticated: boolean;
  isLoading: boolean;
} = {
  hasAnyRole: vi.fn(),
  isAuthenticated: false,
  isLoading: false,
};

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => authState,
}));

function renderProtectedRoute(allowedRoles?: UserRole[]) {
  render(
    <MemoryRouter initialEntries={['/restrito']}>
      <Routes>
        <Route element={<ProtectedRoute allowedRoles={allowedRoles} />} path="/restrito">
          <Route element={<h1>Conteudo restrito</h1>} index />
        </Route>
        <Route element={<h1>Login</h1>} path="/login" />
        <Route element={<h1>Inicio</h1>} path="/" />
      </Routes>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    authState.hasAnyRole.mockReset();
    authState.isAuthenticated = false;
    authState.isLoading = false;
  });

  it('deve redirecionar usuario nao autenticado para o login', () => {
    renderProtectedRoute(['ADMIN']);

    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
  });

  it('deve redirecionar usuario autenticado sem papel permitido para o inicio', () => {
    authState.isAuthenticated = true;
    authState.hasAnyRole.mockReturnValue(false);

    renderProtectedRoute(['ADMIN']);

    expect(screen.getByRole('heading', { name: 'Inicio' })).toBeInTheDocument();
  });

  it('deve renderizar conteudo quando o usuario possuir papel permitido', () => {
    authState.isAuthenticated = true;
    authState.hasAnyRole.mockReturnValue(true);

    renderProtectedRoute(['ADMIN']);

    expect(screen.getByRole('heading', { name: 'Conteudo restrito' })).toBeInTheDocument();
  });
});
