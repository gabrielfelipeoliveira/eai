import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Operacao secundaria responsiva', () => {
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

  test('deve exibir agenda com cards acionaveis no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/follow-ups');

    await expect(page.getByRole('heading', { name: 'Agenda' })).toBeVisible();
    await expect(page.getByText('Retornar contato')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Concluir' })).toBeVisible();
  });

  test('deve exibir leads atrasados em cards no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/leads/overdue');

    await expect(page.getByRole('heading', { name: 'Leads atrasados' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Cliente' })).toHaveCount(0);
    await expect(page.getByText('Cliente Atrasado')).toBeVisible();
    await expect(page.getByText('Toyota Corolla')).toBeVisible();
  });

  test('deve preservar relatorios no desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/reports');

    await expect(page.getByRole('heading', { name: 'Relatorios' })).toBeVisible();
    await expect(page.getByText('Leads por periodo')).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Ana Vendedora' }).first()).toBeVisible();
  });
});
