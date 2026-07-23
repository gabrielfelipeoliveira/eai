import { describe, expect, it } from 'vitest';
import { vehicleLabel } from './leadDisplay';
import type { Lead } from '../../types/lead';

function lead(overrides: Partial<Lead>): Lead {
  return {
    id: 'lead-1',
    companyId: 'company-1',
    storeId: 'store-1',
    customerName: 'Cliente Teste',
    customerPhone: null,
    additionalPhones: [],
    customerEmail: null,
    customerCity: null,
    vehicleInterest: null,
    itemId: null,
    item: null,
    source: 'MANUAL',
    originalMessage: null,
    status: 'AVAILABLE',
    assignedToUserId: null,
    assignedAt: null,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    firstContactAt: null,
    lastContactAt: null,
    lostReason: null,
    saleValue: null,
    saleCurrency: 'BRL',
    relatedLeadId: null,
    overdueToAssign: false,
    overdueToFirstContact: false,
    ...overrides,
  };
}

describe('vehicleLabel', () => {
  it('deve montar label a partir do veiculo estruturado', () => {
    const result = vehicleLabel(
      lead({
        vehicleInterest: 'Texto livre ignorado',
        item: {
          id: 'item-1',
          ownerUserId: 'user-1',
          name: 'Item',
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          vehicle: {
            id: 'vehicle-1',
            itemId: 'item-1',
            name: 'Honda',
            model: 'Civic',
            year: 2024,
            value: null,
            createdAt: '2026-01-01T00:00:00Z',
            updatedAt: '2026-01-01T00:00:00Z',
          },
        },
      }),
    );

    expect(result).toBe('Honda Civic 2024');
  });

  it('deve ignorar partes vazias do veiculo estruturado', () => {
    const result = vehicleLabel(
      lead({
        item: {
          id: 'item-1',
          ownerUserId: 'user-1',
          name: null,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          vehicle: {
            id: 'vehicle-1',
            itemId: 'item-1',
            name: 'Toyota',
            model: null,
            year: 2025,
            value: null,
            createdAt: '2026-01-01T00:00:00Z',
            updatedAt: '2026-01-01T00:00:00Z',
          },
        },
      }),
    );

    expect(result).toBe('Toyota 2025');
  });

  it('deve usar interesse textual quando nao houver veiculo estruturado', () => {
    expect(vehicleLabel(lead({ vehicleInterest: 'Fiat Toro' }))).toBe('Fiat Toro');
  });

  it('deve usar hifen quando nao houver veiculo nem interesse textual', () => {
    expect(vehicleLabel(lead({}))).toBe('-');
  });
});
