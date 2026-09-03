import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Listas responsivas', () => {
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

  test('deve exibir tabela administrativa no desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/stores');

    await expect(page.getByRole('heading', { name: 'Lojas' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Loja' })).toBeVisible();
    await expect(page.getByText('Loja Centro')).toBeVisible();
  });

  test('deve trocar tabela por cards administrativos no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/stores');

    await expect(page.getByRole('heading', { name: 'Lojas' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Loja' })).toHaveCount(0);
    await expect(page.getByText('Loja Centro')).toBeVisible();
    await expect(page.getByText('12345678000190')).toBeVisible();
  });
});
