ALTER TABLE leads ADD COLUMN related_lead_id UUID REFERENCES leads(id) ON DELETE SET NULL;

CREATE INDEX idx_leads_related_lead_id ON leads(related_lead_id);

CREATE TABLE lead_additional_phones (
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    phone VARCHAR(40) NOT NULL,
    PRIMARY KEY (lead_id, phone),
    CONSTRAINT chk_lead_additional_phones_e164 CHECK (
        LENGTH(phone) BETWEEN 9 AND 16
        AND SUBSTRING(phone, 1, 1) = '+'
        AND SUBSTRING(phone, 2, 1) <> '0'
        AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(SUBSTRING(phone, 2), '0', ''), '1', ''), '2', ''), '3', ''), '4', ''), '5', ''), '6', ''), '7', ''), '8', ''), '9', '') = ''
    )
);

CREATE INDEX idx_lead_additional_phones_phone ON lead_additional_phones(phone);
