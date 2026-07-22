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
