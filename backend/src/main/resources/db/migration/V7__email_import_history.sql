CREATE TABLE email_import_history (
    id UUID PRIMARY KEY,
    email_account_id UUID REFERENCES email_accounts(id) ON DELETE SET NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    status VARCHAR(30) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    messages_read INTEGER NOT NULL CHECK (messages_read >= 0),
    leads_created INTEGER NOT NULL CHECK (leads_created >= 0),
    duplicates_marked INTEGER NOT NULL CHECK (duplicates_marked >= 0),
    message TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_email_import_history_account_id ON email_import_history(email_account_id);
CREATE INDEX idx_email_import_history_store_id ON email_import_history(store_id);
CREATE INDEX idx_email_import_history_status ON email_import_history(status);
CREATE INDEX idx_email_import_history_finished_at ON email_import_history(finished_at DESC);
