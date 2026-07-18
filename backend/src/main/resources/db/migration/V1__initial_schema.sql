CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    document VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(180),
    phone VARCHAR(40),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE stores (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(160) NOT NULL,
    document VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(180),
    phone VARCHAR(40),
    city VARCHAR(120),
    state VARCHAR(2),
    address VARCHAR(240),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_stores_company_id ON stores(company_id);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    phone VARCHAR(40),
    job_title VARCHAR(120),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER', 'PRE_SALES', 'F_AND_I', 'AVALIADOR')),
    PRIMARY KEY (user_id, role)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE TABLE leads (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    customer_name VARCHAR(160) NOT NULL,
    customer_phone VARCHAR(40),
    customer_email VARCHAR(180),
    customer_city VARCHAR(120),
    vehicle_interest VARCHAR(180),
    source VARCHAR(30) NOT NULL CHECK (source IN ('MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API', 'WHATSAPP')),
    original_message TEXT,
    status VARCHAR(30) NOT NULL CHECK (status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED')),
    assigned_to_user_id UUID REFERENCES users(id),
    assigned_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    first_contact_at TIMESTAMP WITH TIME ZONE,
    last_contact_at TIMESTAMP WITH TIME ZONE,
    lost_reason VARCHAR(240),
    sale_value NUMERIC(14,2) CHECK (sale_value IS NULL OR sale_value >= 0)
);

CREATE INDEX idx_leads_company_id ON leads(company_id);
CREATE INDEX idx_leads_store_id ON leads(store_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_source ON leads(source);
CREATE INDEX idx_leads_assigned_to_user_id ON leads(assigned_to_user_id);
CREATE INDEX idx_leads_assigned_at ON leads(assigned_at);
CREATE INDEX idx_leads_created_at ON leads(created_at);
CREATE INDEX idx_leads_vehicle_interest ON leads(vehicle_interest);
CREATE INDEX idx_leads_customer_phone ON leads(customer_phone);

CREATE TABLE lead_history (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    previous_status VARCHAR(30) CHECK (previous_status IS NULL OR previous_status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED')),
    new_status VARCHAR(30) NOT NULL CHECK (new_status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lead_history_lead_id ON lead_history(lead_id);
CREATE INDEX idx_lead_history_created_at ON lead_history(created_at DESC);

CREATE TABLE lead_notes (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    note TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lead_notes_lead_id ON lead_notes(lead_id);
CREATE INDEX idx_lead_notes_created_at ON lead_notes(created_at DESC);

CREATE TABLE lead_tags (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL
);

CREATE INDEX idx_lead_tags_lead_id ON lead_tags(lead_id);

CREATE TABLE message_templates (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('FIRST_CONTACT', 'FOLLOW_UP', 'VISIT_INVITE', 'PROPOSAL', 'NO_RESPONSE', 'SOLD', 'LOST')),
    content TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_message_templates_company_id ON message_templates(company_id);
CREATE INDEX idx_message_templates_store_id ON message_templates(store_id);
CREATE INDEX idx_message_templates_active ON message_templates(active);

CREATE TABLE lead_communications (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    channel VARCHAR(30) NOT NULL CHECK (channel IN ('WHATSAPP_LINK', 'WHATSAPP_TEMPLATE')),
    template_id UUID REFERENCES message_templates(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lead_communications_lead_id ON lead_communications(lead_id);
CREATE INDEX idx_lead_communications_created_at ON lead_communications(created_at DESC);

CREATE TABLE whatsapp_contacts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    phone VARCHAR(40) NOT NULL,
    display_name VARCHAR(160),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (store_id, phone)
);

CREATE INDEX idx_whatsapp_contacts_company_id ON whatsapp_contacts(company_id);
CREATE INDEX idx_whatsapp_contacts_store_id ON whatsapp_contacts(store_id);
CREATE INDEX idx_whatsapp_contacts_lead_id ON whatsapp_contacts(lead_id);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    contact_id UUID NOT NULL REFERENCES whatsapp_contacts(id) ON DELETE CASCADE,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    responsible_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (contact_id),
    UNIQUE (lead_id)
);

CREATE INDEX idx_conversations_company_id ON conversations(company_id);
CREATE INDEX idx_conversations_store_id ON conversations(store_id);
CREATE INDEX idx_conversations_lead_id ON conversations(lead_id);
CREATE INDEX idx_conversations_responsible_user_id ON conversations(responsible_user_id);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at DESC);

CREATE TABLE conversation_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    type VARCHAR(20) NOT NULL CHECK (type IN ('TEXT', 'TEMPLATE', 'IMAGE', 'AUDIO', 'DOCUMENT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('RECEIVED', 'SENT', 'DELIVERED', 'READ', 'FAILED')),
    external_message_id VARCHAR(160),
    content TEXT,
    media_id VARCHAR(160),
    media_mime_type VARCHAR(120),
    raw_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_conversation_messages_external_message_id ON conversation_messages(external_message_id);
CREATE INDEX idx_conversation_messages_conversation_id ON conversation_messages(conversation_id);
CREATE INDEX idx_conversation_messages_created_at ON conversation_messages(created_at ASC);
CREATE INDEX idx_conversation_messages_status ON conversation_messages(status);

CREATE TABLE conversation_message_events (
    id UUID PRIMARY KEY,
    message_id UUID REFERENCES conversation_messages(id) ON DELETE SET NULL,
    external_message_id VARCHAR(160) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RECEIVED', 'SENT', 'DELIVERED', 'READ', 'FAILED')),
    failure_reason TEXT,
    raw_payload TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_conversation_message_events_message_id ON conversation_message_events(message_id);
CREATE INDEX idx_conversation_message_events_external_message_id ON conversation_message_events(external_message_id);
CREATE INDEX idx_conversation_message_events_status ON conversation_message_events(status);
CREATE INDEX idx_conversation_message_events_occurred_at ON conversation_message_events(occurred_at DESC);

CREATE TABLE conversation_access_audits (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    actor_role VARCHAR(20) NOT NULL CHECK (actor_role IN ('ADMIN', 'MANAGER')),
    access_type VARCHAR(40) NOT NULL,
    accessed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_conversation_access_audits_conversation_id ON conversation_access_audits(conversation_id);
CREATE INDEX idx_conversation_access_audits_actor_user_id ON conversation_access_audits(actor_user_id);
CREATE INDEX idx_conversation_access_audits_company_id ON conversation_access_audits(company_id);
CREATE INDEX idx_conversation_access_audits_store_id ON conversation_access_audits(store_id);
CREATE INDEX idx_conversation_access_audits_accessed_at ON conversation_access_audits(accessed_at DESC);

CREATE TABLE email_accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    name VARCHAR(120) NOT NULL,
    host VARCHAR(180) NOT NULL,
    port INTEGER NOT NULL CHECK (port > 0 AND port <= 65535),
    username VARCHAR(180) NOT NULL,
    encrypted_password TEXT NOT NULL,
    protocol VARCHAR(20) NOT NULL CHECK (protocol IN ('IMAP')),
    use_ssl BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    last_read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_sync_status VARCHAR(30) NOT NULL CHECK (last_sync_status IN ('NEVER_SYNCED', 'SUCCESS', 'FAILED')),
    last_sync_message TEXT,
    last_sync_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_email_accounts_company_id ON email_accounts(company_id);
CREATE INDEX idx_email_accounts_store_id ON email_accounts(store_id);
CREATE INDEX idx_email_accounts_active ON email_accounts(active);

CREATE TABLE lead_distribution_config (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    mode VARCHAR(30) NOT NULL CHECK (mode IN ('MANUAL', 'ROUND_ROBIN', 'LEAST_BUSY')),
    active BOOLEAN NOT NULL,
    UNIQUE (company_id, store_id)
);

CREATE INDEX idx_lead_distribution_config_store_id ON lead_distribution_config(store_id);

CREATE TABLE lead_sla_policy (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    minutes_to_assign INTEGER NOT NULL CHECK (minutes_to_assign > 0),
    minutes_to_first_contact INTEGER NOT NULL CHECK (minutes_to_first_contact > 0),
    active BOOLEAN NOT NULL,
    UNIQUE (company_id, store_id)
);

CREATE INDEX idx_lead_sla_policy_store_id ON lead_sla_policy(store_id);

CREATE TABLE follow_up_tasks (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(160) NOT NULL,
    description TEXT,
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'DONE', 'CANCELED', 'OVERDUE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_follow_up_tasks_lead_id ON follow_up_tasks(lead_id);
CREATE INDEX idx_follow_up_tasks_user_status_due_at ON follow_up_tasks(user_id, status, due_at);
CREATE INDEX idx_follow_up_tasks_due_at ON follow_up_tasks(due_at);

INSERT INTO companies (id, name, document, email, phone, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'EAI Motors',
    '00000000000191',
    'contato@eaimotors.dev',
    '1130000000',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO stores (id, company_id, name, document, email, phone, city, state, address, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    '00000000-0000-0000-0000-000000000101',
    'EAI Motors Centro',
    '00000000000192',
    'centro@eaimotors.dev',
    '1130000001',
    'Sao Paulo',
    'SP',
    'Avenida Paulista, 1000',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO users (id, name, email, password_hash, phone, job_title, status, company_id, store_id, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Admin EAI', 'admin@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000001', 'Administrador', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000002', 'Marcos Gerente', 'gerente@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000002', 'Gerente comercial', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000011', 'Ana Souza', 'ana@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000011', 'Vendedora', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000012', 'Bruno Lima', 'bruno@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000012', 'Vendedor', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000013', 'Carla Mendes', 'carla@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000013', 'Vendedora', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000031', 'Avaliador EAI', 'avaliador@eai.com', '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6', '11990000031', 'Avaliador', 'ACTIVE', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000002', 'MANAGER'),
    ('00000000-0000-0000-0000-000000000011', 'SELLER'),
    ('00000000-0000-0000-0000-000000000012', 'SELLER'),
    ('00000000-0000-0000-0000-000000000013', 'SELLER'),
    ('00000000-0000-0000-0000-000000000031', 'AVALIADOR');

INSERT INTO message_templates (id, company_id, store_id, name, type, content, active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Primeiro contato', 'FIRST_CONTACT', 'Ola {cliente}, tudo bem? Aqui e {vendedor} da {loja}. Recebemos seu interesse no {veiculo}. Posso te ajudar com mais detalhes?', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Follow-up de negociacao', 'FOLLOW_UP', 'Ola {cliente}, passando para saber se ficou alguma duvida sobre o {veiculo} e se podemos avancar na proposta.', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Convite para visita', 'VISIT_INVITE', 'Ola {cliente}, podemos reservar um horario para voce conhecer o {veiculo} em nossa loja?', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000304', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Envio de proposta', 'PROPOSAL', 'Ola {cliente}, preparei uma proposta para o {veiculo}. Posso te enviar os detalhes por aqui?', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO lead_distribution_config (id, company_id, store_id, mode, active)
VALUES ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'MANUAL', TRUE);

INSERT INTO lead_sla_policy (id, company_id, store_id, minutes_to_assign, minutes_to_first_contact, active)
VALUES ('00000000-0000-0000-0000-000000000402', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 15, 30, TRUE);

INSERT INTO email_accounts (id, company_id, store_id, name, host, port, username, encrypted_password, protocol, use_ssl, active, last_read_at, created_at, updated_at, last_sync_status, last_sync_message, last_sync_at)
VALUES ('00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Leads IMAP Exemplo', 'imap.example.com', 993, 'leads@example.com', 'c2VjcmV0', 'IMAP', TRUE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'NEVER_SYNCED', 'Conta de demonstracao inativa para ambiente local.', NULL);

INSERT INTO leads (id, company_id, store_id, customer_name, customer_phone, customer_email, customer_city, vehicle_interest, source, original_message, status, assigned_to_user_id, assigned_at, created_at, updated_at, first_contact_at, last_contact_at, lost_reason, sale_value)
VALUES
    ('00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Mariana Alves', '11988880001', 'mariana.alves@example.com', 'Sao Paulo', 'Honda Civic Touring 2021', 'WEBSITE', 'Tenho interesse no Civic anunciado no site.', 'NEW', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, NULL, NULL, NULL, NULL),
    ('00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Rafael Costa', '11988880002', 'rafael.costa@example.com', 'Osasco', 'Toyota Corolla XEi 2020', 'MANUAL', 'Cliente entrou na loja e pediu retorno por telefone.', 'NEW', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, NULL, NULL, NULL, NULL),
    ('00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Bianca Rocha', '11988880003', 'bianca.rocha@example.com', 'Guarulhos', 'Jeep Compass Longitude 2022', 'INSTAGRAM', 'Mensagem recebida pelo Instagram solicitando simulacao.', 'FIRST_CONTACT', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, NULL, NULL),
    ('00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Carlos Henrique', '11988880004', 'carlos.henrique@example.com', 'Santo Andre', 'Chevrolet Onix Premier 2023', 'FACEBOOK', 'Lead capturado em campanha do Facebook.', 'FIRST_CONTACT', '00000000-0000-0000-0000-000000000012', CURRENT_TIMESTAMP - INTERVAL '5' HOUR, CURRENT_TIMESTAMP - INTERVAL '6' HOUR, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, NULL, NULL),
    ('00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Fernanda Lima', '11988880005', 'fernanda.lima@example.com', 'Sao Bernardo do Campo', 'Hyundai HB20 Comfort 2022', 'WEBMOTORS', 'Solicitou avaliacao com troca usada.', 'IN_NEGOTIATION', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '20' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, NULL, NULL),
    ('00000000-0000-0000-0000-000000000506', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Paulo Martins', '11988880006', 'paulo.martins@example.com', 'Barueri', 'Fiat Toro Volcano 2021', 'ICARROS', 'Cliente quer agendar visita no sabado.', 'VISIT_SCHEDULED', '00000000-0000-0000-0000-000000000013', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, NULL, NULL),
    ('00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Juliana Nunes', '11988880007', 'juliana.nunes@example.com', 'Sao Caetano do Sul', 'Nissan Kicks Advance 2023', 'OLX', 'Pediu proposta com financiamento em 48 meses.', 'PROPOSAL_SENT', '00000000-0000-0000-0000-000000000012', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, NULL),
    ('00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Eduardo Ramos', '11988880008', 'eduardo.ramos@example.com', 'Sao Paulo', 'Volkswagen T-Cross Highline 2022', 'WEBSITE', 'Lead convertido a partir do site.', 'SOLD', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, 128900.00),
    ('00000000-0000-0000-0000-000000000509', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Patricia Gomes', '11988880009', 'patricia.gomes@example.com', 'Cotia', 'Renault Duster Iconic 2020', 'EMAIL', 'E-mail importado solicitando desconto.', 'LOST', '00000000-0000-0000-0000-000000000013', CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'Cliente comprou em outra loja.', NULL),
    ('00000000-0000-0000-0000-000000000510', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Lucas Pereira', '11988880001', 'lucas.pereira@example.com', 'Sao Paulo', 'Honda Civic Touring 2021', 'EMAIL', 'Mensagem duplicada com telefone ja existente.', 'DUPLICATED', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, NULL, NULL, NULL, NULL);

INSERT INTO lead_history (id, lead_id, user_id, previous_status, new_status, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000801', '00000000-0000-0000-0000-000000000501', NULL, NULL, 'NEW', 'Lead recebido pelo site.', CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
    ('00000000-0000-0000-0000-000000000802', '00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000001', NULL, 'AVAILABLE', 'Lead cadastrado pelo sistema.', CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
    ('00000000-0000-0000-0000-000000000803', '00000000-0000-0000-0000-000000000503', NULL, NULL, 'NEW', 'Lead recebido pelo Instagram.', CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
    ('00000000-0000-0000-0000-000000000804', '00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000011', 'NEW', 'FIRST_CONTACT', 'Primeiro contato realizado por Ana Souza.', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000000805', '00000000-0000-0000-0000-000000000504', NULL, NULL, 'NEW', 'Lead recebido por campanha.', CURRENT_TIMESTAMP - INTERVAL '6' HOUR),
    ('00000000-0000-0000-0000-000000000806', '00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000012', 'ASSIGNED', 'FIRST_CONTACT', 'Primeiro contato realizado por WhatsApp.', CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
    ('00000000-0000-0000-0000-000000000807', '00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000011', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'Cliente enviou dados para simulacao.', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000000808', '00000000-0000-0000-0000-000000000506', '00000000-0000-0000-0000-000000000013', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'Visita agendada na loja.', CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
    ('00000000-0000-0000-0000-000000000809', '00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000012', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'Proposta enviada ao cliente.', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('00000000-0000-0000-0000-000000000810', '00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000011', 'PROPOSAL_SENT', 'SOLD', 'Venda concluida.', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('00000000-0000-0000-0000-000000000811', '00000000-0000-0000-0000-000000000509', '00000000-0000-0000-0000-000000000013', 'IN_NEGOTIATION', 'LOST', 'Cliente comprou em outra loja.', CURRENT_TIMESTAMP - INTERVAL '2' DAY),
    ('00000000-0000-0000-0000-000000000812', '00000000-0000-0000-0000-000000000510', NULL, NULL, 'DUPLICATED', 'Lead marcado como duplicado pelo telefone.', CURRENT_TIMESTAMP - INTERVAL '1' HOUR);

INSERT INTO lead_notes (id, lead_id, user_id, note, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000901', '00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000011', 'Cliente quer usar veiculo atual como entrada.', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000000902', '00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000012', 'Aguardando retorno da financeira.', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('00000000-0000-0000-0000-000000000903', '00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000011', 'Cliente retirou o veiculo com documentacao aprovada.', CURRENT_TIMESTAMP - INTERVAL '1' DAY);

INSERT INTO lead_tags (id, lead_id, name)
VALUES
    ('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000503', 'quente'),
    ('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000505', 'troca'),
    ('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000507', 'financiamento'),
    ('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000509', 'perdido'),
    ('00000000-0000-0000-0000-000000001005', '00000000-0000-0000-0000-000000000510', 'duplicado');

INSERT INTO lead_communications (id, lead_id, user_id, channel, template_id, message, created_at)
VALUES
    ('00000000-0000-0000-0000-000000001101', '00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000012', 'WHATSAPP_LINK', '00000000-0000-0000-0000-000000000301', 'Ola Carlos Henrique, tudo bem? Aqui e Bruno Lima da EAI Motors. Recebemos seu interesse no Chevrolet Onix Premier 2023. Posso te ajudar com mais detalhes?', CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
    ('00000000-0000-0000-0000-000000001102', '00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000011', 'WHATSAPP_LINK', '00000000-0000-0000-0000-000000000302', 'Ola Fernanda Lima, passando para saber se ficou alguma duvida sobre o Hyundai HB20 Comfort 2022 e se podemos avancar na proposta.', CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000001103', '00000000-0000-0000-0000-000000000506', '00000000-0000-0000-0000-000000000013', 'WHATSAPP_LINK', '00000000-0000-0000-0000-000000000303', 'Ola Paulo Martins, podemos reservar um horario para voce conhecer o Fiat Toro Volcano 2021 em nossa loja?', CURRENT_TIMESTAMP - INTERVAL '3' HOUR);

INSERT INTO follow_up_tasks (id, lead_id, user_id, title, description, due_at, completed_at, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000001201', '00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000011', 'Enviar simulacao', 'Enviar primeira simulacao de financiamento para Bianca.', CURRENT_TIMESTAMP + INTERVAL '1' DAY, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000001202', '00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000011', 'Retomar negociacao', 'Confirmar avaliacao do veiculo de entrada.', CURRENT_TIMESTAMP + INTERVAL '4' HOUR, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
    ('00000000-0000-0000-0000-000000001203', '00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000012', 'Cobrar retorno', 'Cliente ainda nao respondeu a proposta enviada.', CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, 'OVERDUE', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('00000000-0000-0000-0000-000000001204', '00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000011', 'Confirmar entrega', 'Confirmar retirada do veiculo vendido.', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'DONE', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    ('00000000-0000-0000-0000-000000001205', '00000000-0000-0000-0000-000000000509', '00000000-0000-0000-0000-000000000013', 'Reativar oportunidade', 'Tarefa cancelada porque o lead foi perdido.', CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, 'CANCELED', CURRENT_TIMESTAMP - INTERVAL '5' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY);
