import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { StoresPage } from './StoresPage';
import { createStore, listStores } from '../services/storeService';

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    hasAnyRole: () => false,
    user: {
      id: 'user-1',
      name: 'Gerente Loja',
      email: 'gerente.loja@eai.com',
      phone: null,
      jobTitle: null,
      companyId: 'company-1',
      storeId: 'store-1',
      status: 'ACTIVE',
      roles: ['STORE_MANAGER'],
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    },
  }),
}));

vi.mock('../services/companyService', () => ({
  listCompanies: vi.fn(),
}));

vi.mock('../services/storeService', () => ({
  createStore: vi.fn(),
  listStores: vi.fn(),
  updateStore: vi.fn(),
}));

function renderStoresPage() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <StoresPage />
    </QueryClientProvider>,
  );
}

describe('StoresPage', () => {
  beforeEach(() => {
    vi.mocked(createStore).mockReset();
    vi.mocked(listStores).mockReset();
    vi.mocked(listStores).mockResolvedValue([
      {
        id: 'store-1',
        companyId: 'company-1',
        name: 'Loja Centro',
        document: '12345678000190',
        email: 'centro@eai.com',
        phone: null,
        city: 'Sao Paulo',
        state: 'SP',
        address: null,
        status: 'ACTIVE',
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
    ]);
  });

  it('deve listar lojas com label de status em PT-BR', async () => {
    renderStoresPage();

    expect(await screen.findByText('Loja Centro')).toBeInTheDocument();
    expect(screen.getByText('Sao Paulo / SP')).toBeInTheDocument();
    expect(screen.getByText('Ativo')).toBeInTheDocument();
  });

  it('deve validar campos obrigatorios antes de cadastrar loja', async () => {
    const user = userEvent.setup();
    renderStoresPage();

    await user.clear(screen.getByLabelText('Nome'));
    await user.clear(screen.getByLabelText('Documento'));
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Informe o nome')).toBeInTheDocument();
    expect(screen.getByText('Informe o documento')).toBeInTheDocument();
    expect(createStore).not.toHaveBeenCalled();
  });

  it('deve enviar cadastro de loja valido com a empresa do usuario autenticado', async () => {
    const user = userEvent.setup();
    vi.mocked(createStore).mockResolvedValue({
      id: 'store-2',
      companyId: 'company-1',
      name: 'Loja Norte',
      document: '98765432000110',
      email: null,
      phone: null,
      city: null,
      state: null,
      address: null,
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    });
    renderStoresPage();

    await user.clear(screen.getByLabelText('Nome'));
    await user.type(screen.getByLabelText('Nome'), 'Loja Norte');
    await user.clear(screen.getByLabelText('Documento'));
    await user.type(screen.getByLabelText('Documento'), '98765432000110');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    await waitFor(() =>
      expect(createStore).toHaveBeenCalledWith(
        expect.objectContaining({
          companyId: 'company-1',
          document: '98765432000110',
          name: 'Loja Norte',
          status: 'ACTIVE',
        }),
      ),
    );
  });
});
