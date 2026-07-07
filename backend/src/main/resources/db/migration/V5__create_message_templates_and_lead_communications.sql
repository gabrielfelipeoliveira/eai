CREATE TABLE message_templates (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT message_templates_type_check CHECK (type IN ('FIRST_CONTACT', 'FOLLOW_UP', 'VISIT_INVITE', 'PROPOSAL', 'NO_RESPONSE', 'SOLD', 'LOST'))
);

CREATE TABLE lead_communications (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    channel VARCHAR(30) NOT NULL,
    template_id UUID NOT NULL REFERENCES message_templates(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT lead_communications_channel_check CHECK (channel IN ('WHATSAPP_LINK'))
);

CREATE INDEX idx_message_templates_company_id ON message_templates(company_id);
CREATE INDEX idx_message_templates_store_id ON message_templates(store_id);
CREATE INDEX idx_message_templates_active ON message_templates(active);
CREATE INDEX idx_lead_communications_lead_id_created_at ON lead_communications(lead_id, created_at DESC);

INSERT INTO message_templates (
    id,
    company_id,
    store_id,
    name,
    type,
    content,
    active,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000301',
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000201',
    'Primeiro contato',
    'FIRST_CONTACT',
    'Olá {cliente}, tudo bem? Vi que você demonstrou interesse no veículo {veiculo}. Sou {vendedor}, da {loja}. Posso te passar mais informações?',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
