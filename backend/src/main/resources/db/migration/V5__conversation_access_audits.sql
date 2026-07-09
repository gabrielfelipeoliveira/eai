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
