CREATE TABLE leads (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    customer_name VARCHAR(160) NOT NULL,
    customer_phone VARCHAR(40),
    customer_email VARCHAR(180),
    customer_city VARCHAR(120),
    vehicle_interest VARCHAR(180),
    source VARCHAR(30) NOT NULL,
    original_message TEXT,
    status VARCHAR(30) NOT NULL,
    assigned_to_user_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    first_contact_at TIMESTAMP WITH TIME ZONE,
    last_contact_at TIMESTAMP WITH TIME ZONE,
    lost_reason VARCHAR(240),
    sale_value NUMERIC(14, 2),
    CONSTRAINT leads_source_check CHECK (source IN ('MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API')),
    CONSTRAINT leads_status_check CHECK (status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED')),
    CONSTRAINT leads_sale_value_check CHECK (sale_value IS NULL OR sale_value >= 0)
);

CREATE TABLE lead_history (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT lead_history_previous_status_check CHECK (previous_status IS NULL OR previous_status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED')),
    CONSTRAINT lead_history_new_status_check CHECK (new_status IN ('NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', 'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED'))
);

CREATE TABLE lead_notes (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    note TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE lead_tags (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL
);

CREATE INDEX idx_leads_company_id ON leads(company_id);
CREATE INDEX idx_leads_store_id ON leads(store_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_source ON leads(source);
CREATE INDEX idx_leads_assigned_to_user_id ON leads(assigned_to_user_id);
CREATE INDEX idx_leads_created_at ON leads(created_at);
CREATE INDEX idx_leads_vehicle_interest ON leads(vehicle_interest);
CREATE INDEX idx_leads_customer_phone ON leads(customer_phone);
CREATE INDEX idx_lead_history_lead_id_created_at ON lead_history(lead_id, created_at DESC);
CREATE INDEX idx_lead_notes_lead_id_created_at ON lead_notes(lead_id, created_at DESC);
CREATE INDEX idx_lead_tags_lead_id ON lead_tags(lead_id);
