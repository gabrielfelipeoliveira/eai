import { describe, expect, it } from 'vitest';
import { apiErrorCode, apiErrorMessage } from './api';

describe('api error helpers', () => {
  it('deve ignorar erros vazios ou sem response', () => {
    expect(apiErrorCode(null)).toBeUndefined();
    expect(apiErrorCode(undefined)).toBeUndefined();
    expect(apiErrorCode(new Error('falha'))).toBeUndefined();
    expect(apiErrorMessage(null)).toBeUndefined();
    expect(apiErrorMessage(undefined)).toBeUndefined();
    expect(apiErrorMessage(new Error('falha'))).toBeUndefined();
  });

  it('deve extrair codigo e mensagem de erros da API', () => {
    const error = {
      response: {
        data: {
          code: 'WHATSAPP_FREE_TEXT_WINDOW_EXPIRED',
          message: 'Janela expirada',
        },
      },
    };

    expect(apiErrorCode(error)).toBe('WHATSAPP_FREE_TEXT_WINDOW_EXPIRED');
    expect(apiErrorMessage(error)).toBe('Janela expirada');
  });
});
