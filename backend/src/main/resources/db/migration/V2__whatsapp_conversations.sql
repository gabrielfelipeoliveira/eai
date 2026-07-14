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
