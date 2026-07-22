import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Fluxos E2E de leads', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page);
    await page.goto('/login');
    await page.getByLabel('E-mail').fill('admin@eai.com');
    await page.getByLabel('Senha').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
  });

  test('deve listar e filtrar leads criticos do MVP', async ({ page }) => {
    await page.getByRole('link', { name: /Leads/ }).click();

    await expect(page.getByRole('heading', { name: 'Leads', exact: true })).toBeVisible();
    await expect(page.getByText('Cliente Inicial')).toBeVisible();

    await page.getByLabel('Texto livre').fill('Cliente Inicial');
    await page.getByRole('button', { name: 'Filtrar' }).click();

    await expect(page.getByText('Cliente Inicial')).toBeVisible();
    await expect(page.getByText('Honda Civic')).toBeVisible();
  });

  test('deve criar lead manual pela UI e atualizar a listagem', async ({ page }) => {
    await page.goto('/leads');
    await page.getByRole('button', { name: 'Novo lead' }).click();

    await expect(page.getByRole('heading', { name: 'Novo lead' })).toBeVisible();
    await page.getByLabel('Cliente').fill('Cliente E2E');
    await page.getByRole('textbox', { name: 'Telefone' }).fill('11988887777');
    await page.getByLabel('Veiculo de interesse').fill('Toyota Corolla');
    await page.getByRole('button', { name: 'Criar lead' }).click();

    await expect(page.getByRole('heading', { name: 'Novo lead' })).toHaveCount(0);
    await expect(page.getByText('Cliente E2E')).toBeVisible();
    await expect(page.getByText('Toyota Corolla')).toBeVisible();
  });
});
