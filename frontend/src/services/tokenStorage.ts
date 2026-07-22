import type { AuthTokens } from '../types/auth';

let accessToken: string | null = null;
const legacyAccessTokenKey = 'eai.accessToken';
const legacyRefreshTokenKey = 'eai.refreshToken';

export function getAccessToken() {
  return accessToken;
}

export function saveTokens(tokens: AuthTokens) {
  accessToken = tokens.accessToken;
  clearLegacyStoredTokens();
}

export function clearTokens() {
  accessToken = null;
  clearLegacyStoredTokens();
}

function clearLegacyStoredTokens() {
  localStorage.removeItem(legacyAccessTokenKey);
  localStorage.removeItem(legacyRefreshTokenKey);
}
