import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Fluxos E2E de autenticacao e autorizacao', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page);
  });

  test('deve redirecionar rota protegida para login quando usuario nao estiver autenticado', async ({ page }) => {
    await page.goto('/leads');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('heading', { name: 'EAI' })).toBeVisible();
  });

  test('deve autenticar administrador e exibir navegacao administrativa', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('E-mail').fill('admin@eai.com');
    await page.getByLabel('Senha').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByRole('link', { name: /Usuarios/ })).toBeVisible();
    await expect(page.getByRole('link', { name: /Lojas/ })).toBeVisible();
  });

  test('deve bloquear tela administrativa para usuario sem papel permitido', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('E-mail').fill('ana@eai.com');
    await page.getByLabel('Senha').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await page.goto('/stores');

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByRole('link', { name: /Lojas/ })).toHaveCount(0);
  });
});
