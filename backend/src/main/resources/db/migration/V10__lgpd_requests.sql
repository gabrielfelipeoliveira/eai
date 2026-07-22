CREATE TABLE lgpd_requests (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID REFERENCES stores(id),
    lead_id UUID REFERENCES leads(id),
    data_subject_name VARCHAR(160) NOT NULL,
    data_subject_phone VARCHAR(40),
    data_subject_email VARCHAR(180),
    request_type VARCHAR(30) NOT NULL CHECK (request_type IN ('ACCESS', 'CORRECTION', 'BLOCK', 'ANONYMIZATION', 'DELETION')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'REJECTED')),
    description TEXT NOT NULL,
    requested_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE lgpd_request_actions (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES lgpd_requests(id) ON DELETE CASCADE,
    executor_user_id UUID NOT NULL REFERENCES users(id),
    action_type VARCHAR(30) NOT NULL CHECK (action_type IN ('ACCESS', 'CORRECTION', 'BLOCK', 'ANONYMIZATION', 'DELETION')),
    resolution TEXT NOT NULL,
    final_status VARCHAR(30) CHECK (final_status IS NULL OR final_status IN ('COMPLETED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lgpd_requests_company_id ON lgpd_requests(company_id);
CREATE INDEX idx_lgpd_requests_store_id ON lgpd_requests(store_id);
CREATE INDEX idx_lgpd_requests_lead_id ON lgpd_requests(lead_id);
CREATE INDEX idx_lgpd_requests_status ON lgpd_requests(status);
CREATE INDEX idx_lgpd_requests_created_at ON lgpd_requests(created_at DESC);
CREATE INDEX idx_lgpd_request_actions_request_id ON lgpd_request_actions(request_id);
CREATE INDEX idx_lgpd_request_actions_executor_user_id ON lgpd_request_actions(executor_user_id);
