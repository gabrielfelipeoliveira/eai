ALTER TABLE leads ADD COLUMN assigned_at TIMESTAMP WITH TIME ZONE;

UPDATE leads
SET assigned_at = updated_at
WHERE assigned_to_user_id IS NOT NULL
  AND assigned_at IS NULL;

CREATE TABLE lead_distribution_config (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    mode VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT lead_distribution_mode_check CHECK (mode IN ('MANUAL', 'ROUND_ROBIN', 'LEAST_BUSY')),
    CONSTRAINT uq_lead_distribution_store UNIQUE (company_id, store_id)
);

CREATE TABLE lead_sla_policy (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    minutes_to_assign INTEGER NOT NULL,
    minutes_to_first_contact INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT lead_sla_minutes_to_assign_check CHECK (minutes_to_assign > 0),
    CONSTRAINT lead_sla_minutes_to_first_contact_check CHECK (minutes_to_first_contact > 0),
    CONSTRAINT uq_lead_sla_policy_store UNIQUE (company_id, store_id)
);

CREATE INDEX idx_leads_assigned_at ON leads(assigned_at);
CREATE INDEX idx_lead_distribution_config_store_id ON lead_distribution_config(store_id);
CREATE INDEX idx_lead_sla_policy_store_id ON lead_sla_policy(store_id);
