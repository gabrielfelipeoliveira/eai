CREATE TABLE items (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(180),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_items_owner_user_id ON items(owner_user_id);

CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    name VARCHAR(180),
    "year" INTEGER,
    model VARCHAR(120),
    "value" NUMERIC(14,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_vehicles_value_non_negative CHECK ("value" IS NULL OR "value" >= 0)
);

CREATE INDEX idx_vehicles_item_id ON vehicles(item_id);

ALTER TABLE leads ADD COLUMN item_id UUID REFERENCES items(id);
ALTER TABLE leads ADD COLUMN sale_currency VARCHAR(3) NOT NULL DEFAULT 'BRL';

UPDATE leads
SET customer_phone = '+' || customer_phone
WHERE customer_phone IS NOT NULL
  AND customer_phone NOT LIKE '+%'
  AND SUBSTRING(customer_phone, 1, 2) = '55'
  AND LENGTH(customer_phone) BETWEEN 8 AND 15;

UPDATE leads
SET customer_phone = '+55' || customer_phone
WHERE customer_phone IS NOT NULL
  AND customer_phone NOT LIKE '+%'
  AND LENGTH(customer_phone) IN (10, 11);

ALTER TABLE leads ADD CONSTRAINT chk_leads_sale_value_non_negative CHECK (sale_value IS NULL OR sale_value >= 0);
ALTER TABLE leads ADD CONSTRAINT chk_leads_sale_currency_iso3 CHECK (
    LENGTH(sale_currency) = 3
    AND sale_currency = UPPER(sale_currency)
    AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sale_currency, 'A', ''), 'B', ''), 'C', ''), 'D', ''), 'E', ''), 'F', ''), 'G', ''), 'H', ''), 'I', ''), 'J', ''), 'K', ''), 'L', ''), 'M', ''), 'N', ''), 'O', ''), 'P', ''), 'Q', ''), 'R', ''), 'S', ''), 'T', ''), 'U', ''), 'V', ''), 'W', ''), 'X', ''), 'Y', ''), 'Z', '') = ''
);
ALTER TABLE leads ADD CONSTRAINT chk_leads_customer_phone_e164 CHECK (
    customer_phone IS NULL
    OR (
        LENGTH(customer_phone) BETWEEN 9 AND 16
        AND SUBSTRING(customer_phone, 1, 1) = '+'
        AND SUBSTRING(customer_phone, 2, 1) <> '0'
        AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(SUBSTRING(customer_phone, 2), '0', ''), '1', ''), '2', ''), '3', ''), '4', ''), '5', ''), '6', ''), '7', ''), '8', ''), '9', '') = ''
    )
);

ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_status_check;
ALTER TABLE leads ADD CONSTRAINT leads_status_check CHECK (
    status IN (
        'NEW',
        'AVAILABLE',
        'ASSIGNED',
        'FIRST_CONTACT',
        'IN_NEGOTIATION',
        'VISIT_SCHEDULED',
        'SIMULATING',
        'PROPOSAL_APPROVED',
        'PROPOSAL_SENT',
        'SOLD',
        'LOST',
        'DUPLICATED'
    )
);

CREATE INDEX idx_leads_item_id ON leads(item_id);
