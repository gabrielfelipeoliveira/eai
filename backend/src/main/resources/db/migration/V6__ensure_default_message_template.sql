INSERT INTO message_templates (
    id,
    company_id,
    store_id,
    name,
    type,
    content,
    active,
    created_at,
    updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000301',
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000201',
    'Primeiro contato',
    'FIRST_CONTACT',
    'Olá {cliente}, tudo bem? Vi que você demonstrou interesse no veículo {veiculo}. Sou {vendedor}, da {loja}. Posso te passar mais informações?',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM message_templates
    WHERE id = '00000000-0000-0000-0000-000000000301'
);
