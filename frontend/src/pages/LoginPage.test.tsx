import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';

const loginMock = vi.fn();

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    isAuthenticated: false,
    login: loginMock,
  }),
}));

function renderLoginPage() {
  render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route element={<LoginPage />} path="/login" />
        <Route element={<h1>Inicio autenticado</h1>} path="/" />
      </Routes>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    loginMock.mockReset();
  });

  it('deve exibir mensagens de validacao em PT-BR quando credenciais forem invalidas', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.clear(screen.getByLabelText('E-mail'));
    await user.clear(screen.getByLabelText('Senha'));
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Informe um e-mail valido')).toBeInTheDocument();
    expect(screen.getByText('Informe a senha')).toBeInTheDocument();
    expect(loginMock).not.toHaveBeenCalled();
  });

  it('deve autenticar e redirecionar para o inicio quando login for aceito', async () => {
    const user = userEvent.setup();
    loginMock.mockResolvedValue(undefined);
    renderLoginPage();

    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() => expect(loginMock).toHaveBeenCalledWith('admin@eai.com', 'admin123'));
    expect(await screen.findByRole('heading', { name: 'Inicio autenticado' })).toBeInTheDocument();
  });

  it('deve informar erro de autenticacao quando o backend rejeitar as credenciais', async () => {
    const user = userEvent.setup();
    loginMock.mockRejectedValue(new Error('unauthorized'));
    renderLoginPage();

    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('E-mail ou senha invalidos.')).toBeInTheDocument();
  });
});
