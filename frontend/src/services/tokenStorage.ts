import type { AuthTokens } from '../types/auth';

const accessTokenKey = 'eai.accessToken';
const refreshTokenKey = 'eai.refreshToken';

export function getAccessToken() {
  return localStorage.getItem(accessTokenKey);
}

export function getRefreshToken() {
  return localStorage.getItem(refreshTokenKey);
}

export function saveTokens(tokens: AuthTokens) {
  localStorage.setItem(accessTokenKey, tokens.accessToken);
  localStorage.setItem(refreshTokenKey, tokens.refreshToken);
}

export function clearTokens() {
  localStorage.removeItem(accessTokenKey);
  localStorage.removeItem(refreshTokenKey);
}
