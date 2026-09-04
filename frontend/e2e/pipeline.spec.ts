import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Pipeline responsivo', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page);
  });

  async function loginAsAdmin(page: Parameters<typeof mockApi>[0]) {
    await page.goto('/login');
    await page.getByLabel('E-mail').fill('admin@eai.com');
    await page.getByLabel('Senha').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL('/');
  }

  test('deve exibir leads agrupados por etapa no desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/pipeline');

    await expect(page.getByRole('heading', { name: 'Pipeline' })).toBeVisible();
    await expect(page.getByRole('region', { name: 'Etapa Disponivel' })).toBeVisible();
    await expect(page.getByRole('button', { name: /Cliente Inicial/ })).toBeVisible();
    await expect(page.getByText('Honda Civic')).toBeVisible();
  });

  test('deve manter kanban horizontal no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/pipeline');

    await expect(page.getByRole('heading', { name: 'Pipeline' })).toBeVisible();
    await expect(page.getByRole('region', { name: 'Etapa Disponivel' })).toBeVisible();
    await expect(page.getByRole('button', { name: /Cliente Inicial/ })).toBeVisible();
  });
});
