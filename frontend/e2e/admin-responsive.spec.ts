import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Administracao responsiva', () => {
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

  test('deve exibir templates em tabela no desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/templates');

    await expect(page.getByRole('heading', { name: 'Templates' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Nome Meta' })).toBeVisible();
    await expect(page.getByText('primeiro_contato')).toBeVisible();
  });

  test('deve exibir templates em cards no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/templates');

    await expect(page.getByRole('heading', { name: 'Templates' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Nome Meta' })).toHaveCount(0);
    await expect(page.getByText('primeiro_contato')).toBeVisible();
    await expect(page.getByText('Ola {cliente}, tudo bem? Aqui e {vendedor}.')).toBeVisible();
  });

  test('deve exibir contas de e-mail em cards no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/email-accounts');

    await expect(page.getByRole('heading', { name: 'Contas de E-mail' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Conta' })).toHaveCount(0);
    await expect(page.getByText('Leads IMAP')).toBeVisible();
    await expect(page.getByText('imap.example.com:993')).toBeVisible();
  });
});
