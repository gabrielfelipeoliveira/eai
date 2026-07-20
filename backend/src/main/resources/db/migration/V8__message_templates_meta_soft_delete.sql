ALTER TABLE message_templates
    ALTER COLUMN store_id DROP NOT NULL;

ALTER TABLE message_templates
    ADD COLUMN language_code VARCHAR(20) NOT NULL DEFAULT 'pt-BR';

ALTER TABLE message_templates
    ADD COLUMN meta_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED';

ALTER TABLE message_templates
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE message_templates
    ADD CONSTRAINT chk_message_templates_meta_status CHECK (meta_status IN ('PENDING', 'APPROVED', 'REJECTED', 'PAUSED', 'DISABLED'));

UPDATE message_templates
   SET name = CASE id
       WHEN '00000000-0000-0000-0000-000000000301' THEN 'primeiro_contato'
       WHEN '00000000-0000-0000-0000-000000000302' THEN 'follow_up_negociacao'
       WHEN '00000000-0000-0000-0000-000000000303' THEN 'convite_visita'
       WHEN '00000000-0000-0000-0000-000000000304' THEN 'envio_proposta'
       ELSE lower(regexp_replace(name, '[^a-zA-Z0-9]+', '_', 'g'))
   END;

CREATE INDEX idx_message_templates_meta_status ON message_templates(meta_status);
CREATE INDEX idx_message_templates_deleted_at ON message_templates(deleted_at);
