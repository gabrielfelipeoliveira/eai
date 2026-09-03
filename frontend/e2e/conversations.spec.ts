import { expect, test } from '@playwright/test';
import { mockApi } from './fixtures/api';

test.describe('Conversas responsivas', () => {
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

  test('deve exibir conversa, mensagens e composer no desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/conversations');

    await expect(page.getByRole('heading', { name: 'Conversas' })).toBeVisible();
    await expect(page.getByText('Caixa de entrada')).toBeVisible();
    await expect(page.getByRole('button', { name: /Cliente Inicial/ })).toBeVisible();
    await expect(page.getByText('Ola, tenho interesse no Honda Civic.')).toBeVisible();
    await expect(page.getByText('Combinado, vou enviar os detalhes.').nth(1)).toBeVisible();
    await expect(page.getByPlaceholder('Digite uma mensagem')).toBeEnabled();
  });

  test('deve manter atendimento navegavel no mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await loginAsAdmin(page);

    await page.goto('/conversations');

    await expect(page.getByRole('heading', { name: 'Conversas' })).toBeVisible();
    await expect(page.getByText('Caixa de entrada')).toBeVisible();
    await expect(page.getByRole('button', { name: /Cliente Inicial/ })).toBeVisible();
    await expect(page.getByText('Ola, tenho interesse no Honda Civic.')).toBeVisible();
  });

  test('deve enviar mensagem de texto pela conversa selecionada', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await loginAsAdmin(page);

    await page.goto('/conversations');

    await page.getByPlaceholder('Digite uma mensagem').fill('Mensagem enviada pelo e2e');
    await page.getByRole('button', { name: 'Enviar', exact: true }).click();

    await expect(page.getByText('Mensagem enviada pelo e2e')).toBeVisible();
  });
});
