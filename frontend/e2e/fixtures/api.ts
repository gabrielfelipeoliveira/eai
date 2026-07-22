import type { Page, Route } from '@playwright/test';

type Role = 'ADMIN' | 'MANAGER' | 'STORE_MANAGER' | 'SELLER' | 'PRE_SALES' | 'F_AND_I' | 'AVALIADOR';

interface MockUser {
  id: string;
  name: string;
  email: string;
  companyId: string;
  storeId: string;
  roles: Role[];
}

interface MockLead {
  id: string;
  companyId: string;
  storeId: string;
  customerName: string;
  customerPhone: string | null;
  additionalPhones: string[];
  customerEmail: string | null;
  customerCity: string | null;
  vehicleInterest: string | null;
  itemId: string | null;
  item: {
    id: string;
    ownerUserId: string;
    name: string | null;
    vehicle: {
      id: string;
      itemId: string;
      name: string | null;
      year: number | null;
      model: string | null;
      value: number | null;
      createdAt: string;
      updatedAt: string;
    } | null;
    createdAt: string;
    updatedAt: string;
  } | null;
  source: string;
  originalMessage: string | null;
  status: string;
  assignedToUserId: string | null;
  assignedAt: string | null;
  createdAt: string;
  updatedAt: string;
  firstContactAt: string | null;
  lastContactAt: string | null;
  lostReason: string | null;
  saleValue: number | null;
  saleCurrency: string;
  relatedLeadId: string | null;
  overdueToAssign: boolean;
  overdueToFirstContact: boolean;
}

const adminUser: MockUser = {
  id: 'user-admin',
  name: 'Admin EAI',
  email: 'admin@eai.com',
  companyId: 'company-1',
  storeId: 'store-1',
  roles: ['ADMIN'],
};

const sellerUser: MockUser = {
  id: 'user-seller',
  name: 'Ana Vendedora',
  email: 'ana@eai.com',
  companyId: 'company-1',
  storeId: 'store-1',
  roles: ['SELLER'],
};

const stores = [
  {
    id: 'store-1',
    companyId: 'company-1',
    name: 'Loja Centro',
    document: '12345678000190',
    email: 'centro@eai.com',
    phone: null,
    city: 'Sao Paulo',
    state: 'SP',
    address: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
];

const companies = [
  {
    id: 'company-1',
    name: 'Grupo EAI',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
];

export async function mockApi(page: Page) {
  let authenticatedUser: MockUser | null = null;
  const leads: MockLead[] = [
    lead({
      id: 'lead-1',
      customerName: 'Cliente Inicial',
      customerPhone: '+5511999990000',
      vehicleInterest: 'Honda Civic',
      status: 'AVAILABLE',
      source: 'MANUAL',
    }),
  ];

  await page.route('http://localhost:8080/api/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname.replace('/api', '');
    const method = request.method();

    if (path === '/auth/refresh') {
      return authenticatedUser ? json(route, authTokens()) : json(route, { message: 'Unauthorized' }, 401);
    }

    if (path === '/auth/login' && method === 'POST') {
      const body = request.postDataJSON() as { email?: string };
      authenticatedUser = body.email === 'ana@eai.com' ? sellerUser : adminUser;
      return json(route, authTokens());
    }

    if (path === '/auth/logout' && method === 'POST') {
      authenticatedUser = null;
      return json(route, {});
    }

    if (path === '/auth/me') {
      return authenticatedUser ? json(route, toAuthUser(authenticatedUser)) : json(route, { message: 'Unauthorized' }, 401);
    }

    if (path === '/metadata') {
      return json(route, metadata());
    }

    if (!authenticatedUser) {
      return json(route, { message: 'Unauthorized' }, 401);
    }

    if (path === '/companies') {
      return json(route, companies);
    }

    if (path === '/stores') {
      return json(route, stores);
    }

    if (path === '/users') {
      return json(route, [toAuthUser(adminUser), toAuthUser(sellerUser)]);
    }

    if (path === '/leads' && method === 'GET') {
      const text = url.searchParams.get('text')?.toLowerCase();
      const content = text ? leads.filter((item) => item.customerName.toLowerCase().includes(text)) : leads;
      return json(route, { content, page: 0, size: 10, totalElements: content.length, totalPages: 1 });
    }

    if (path === '/leads' && method === 'POST') {
      const body = request.postDataJSON() as Partial<MockLead>;
      const created = lead({
        id: `lead-${leads.length + 1}`,
        customerName: body.customerName ?? 'Cliente sem nome',
        customerPhone: body.customerPhone ?? null,
        customerEmail: body.customerEmail ?? null,
        vehicleInterest: body.vehicleInterest ?? null,
        source: body.source ?? 'MANUAL',
      });
      leads.unshift(created);
      return json(route, created, 201);
    }

    if (path.startsWith('/dashboard/')) {
      return json(route, dashboardResponse(path));
    }

    if (path === '/notifications/unread-count') {
      return json(route, { count: 0 });
    }

    return json(route, {});
  });
}

function json(route: Route, body: unknown, status = 200) {
  return route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
}

function authTokens() {
  return { accessToken: 'mock-access-token', tokenType: 'Bearer' };
}

function toAuthUser(user: MockUser) {
  return {
    ...user,
    phone: null,
    jobTitle: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };
}

function lead(overrides: Partial<MockLead>): MockLead {
  return {
    id: 'lead-1',
    companyId: 'company-1',
    storeId: 'store-1',
    customerName: 'Cliente',
    customerPhone: null,
    additionalPhones: [],
    customerEmail: null,
    customerCity: null,
    vehicleInterest: null,
    itemId: null,
    item: null,
    source: 'MANUAL',
    originalMessage: null,
    status: 'NEW',
    assignedToUserId: null,
    assignedAt: null,
    createdAt: '2026-07-22T12:00:00Z',
    updatedAt: '2026-07-22T12:00:00Z',
    firstContactAt: null,
    lastContactAt: null,
    lostReason: null,
    saleValue: null,
    saleCurrency: 'BRL',
    relatedLeadId: null,
    overdueToAssign: false,
    overdueToFirstContact: false,
    ...overrides,
  };
}

function dashboardResponse(path: string) {
  if (path === '/dashboard/summary') {
    return {
      totalLeadsToday: 1,
      totalLeadsThisMonth: 3,
      availableLeads: 1,
      assignedLeads: 1,
      soldLeads: 1,
      lostLeads: 0,
      conversionRate: 33.3,
      averageFirstResponseTimeMinutes: 15,
    };
  }

  if (path === '/dashboard/leads-by-seller') {
    return [{ sellerId: 'user-seller', sellerName: 'Ana Vendedora', leadCount: 2, soldLeads: 1, conversionRate: 50 }];
  }

  if (path === '/dashboard/sales-by-period') {
    return [{ period: '2026-07', soldLeads: 1, saleValue: 100000 }];
  }

  return [{ label: 'MANUAL', value: 1 }];
}

function metadata() {
  const option = (code: string, label: string, order: number, color = 'default') => ({
    code,
    labelKey: code.toLowerCase(),
    label,
    order,
    color,
  });

  return {
    locale: 'pt-BR',
    leadStatuses: [
      option('NEW', 'Novo', 1, 'info'),
      option('AVAILABLE', 'Disponivel', 2, 'primary'),
      option('ASSIGNED', 'Atribuido', 3, 'secondary'),
      option('FIRST_CONTACT', 'Primeiro contato', 4, 'warning'),
      option('IN_NEGOTIATION', 'Em negociacao', 5, 'warning'),
      option('VISIT_SCHEDULED', 'Visita agendada', 6, 'info'),
      option('SIMULATING', 'Simulacao', 7, 'info'),
      option('PROPOSAL_APPROVED', 'Proposta aprovada', 8, 'secondary'),
      option('PROPOSAL_SENT', 'Proposta enviada', 9, 'secondary'),
      option('SOLD', 'Vendido', 10, 'success'),
      option('LOST', 'Perdido', 11, 'error'),
      option('DUPLICATED', 'Duplicado', 12),
    ],
    leadSources: [
      option('MANUAL', 'Manual', 1),
      option('EMAIL', 'E-mail', 2),
      option('WEBSITE', 'Site', 3),
      option('FACEBOOK', 'Facebook', 4),
      option('INSTAGRAM', 'Instagram', 5),
      option('WEBMOTORS', 'Webmotors', 6),
      option('ICARROS', 'iCarros', 7),
      option('OLX', 'OLX', 8),
      option('API', 'API', 9),
    ],
    followUpStatuses: [],
    userRoles: [
      option('ADMIN', 'Administrador', 1, 'error'),
      option('SELLER', 'Vendedor', 2, 'success'),
    ],
    userStatuses: [],
    tenantStatuses: [option('ACTIVE', 'Ativo', 1, 'success'), option('INACTIVE', 'Inativo', 2)],
    messageTemplateTypes: [],
    messageTemplateMetaStatuses: [],
    leadDistributionModes: [],
    emailAccountStatuses: [],
    emailProtocols: [],
    conversationMessageDirections: [],
    conversationMessageTypes: [],
    conversationMessageStatuses: [],
  };
}
