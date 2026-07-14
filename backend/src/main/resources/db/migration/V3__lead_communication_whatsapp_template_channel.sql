ALTER TABLE lead_communications DROP CONSTRAINT IF EXISTS lead_communications_channel_check;

ALTER TABLE lead_communications
    ADD CONSTRAINT lead_communications_channel_check
    CHECK (channel IN ('WHATSAPP_LINK', 'WHATSAPP_TEMPLATE'));
