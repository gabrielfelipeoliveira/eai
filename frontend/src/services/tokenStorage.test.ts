import { beforeEach, describe, expect, it } from 'vitest';
import { clearTokens, getAccessToken, saveTokens } from './tokenStorage';

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear();
    clearTokens();
  });

  it('deve manter access token apenas em memoria', () => {
    saveTokens({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
    });

    expect(getAccessToken()).toBe('access-token');
    expect(localStorage.getItem('eai.accessToken')).toBeNull();
    expect(localStorage.getItem('eai.refreshToken')).toBeNull();
  });

  it('deve remover tokens legados ao salvar novos tokens', () => {
    localStorage.setItem('eai.accessToken', 'legacy-access-token');
    localStorage.setItem('eai.refreshToken', 'legacy-refresh-token');

    saveTokens({
      accessToken: 'novo-access-token',
      tokenType: 'Bearer',
    });

    expect(getAccessToken()).toBe('novo-access-token');
    expect(localStorage.getItem('eai.accessToken')).toBeNull();
    expect(localStorage.getItem('eai.refreshToken')).toBeNull();
  });

  it('deve limpar token em memoria e tokens legados', () => {
    saveTokens({
      accessToken: 'access-token',
      tokenType: 'Bearer',
    });
    localStorage.setItem('eai.accessToken', 'legacy-access-token');
    localStorage.setItem('eai.refreshToken', 'legacy-refresh-token');

    clearTokens();

    expect(getAccessToken()).toBeNull();
    expect(localStorage.getItem('eai.accessToken')).toBeNull();
    expect(localStorage.getItem('eai.refreshToken')).toBeNull();
  });
});
