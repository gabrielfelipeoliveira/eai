ALTER TABLE lead_history ALTER COLUMN user_id DROP NOT NULL;

CREATE TABLE email_accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    name VARCHAR(120) NOT NULL,
    host VARCHAR(180) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(180) NOT NULL,
    encrypted_password TEXT NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    use_ssl BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    last_read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_sync_status VARCHAR(30) NOT NULL,
    last_sync_message TEXT,
    last_sync_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT email_accounts_protocol_check CHECK (protocol IN ('IMAP')),
    CONSTRAINT email_accounts_status_check CHECK (last_sync_status IN ('NEVER_SYNCED', 'SUCCESS', 'FAILED')),
    CONSTRAINT email_accounts_port_check CHECK (port BETWEEN 1 AND 65535)
);

CREATE INDEX idx_email_accounts_company_id ON email_accounts(company_id);
CREATE INDEX idx_email_accounts_store_id ON email_accounts(store_id);
CREATE INDEX idx_email_accounts_active ON email_accounts(active);
