CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    document VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(180),
    phone VARCHAR(40),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT companies_status_check CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE stores (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(160) NOT NULL,
    document VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(180),
    phone VARCHAR(40),
    city VARCHAR(120),
    state VARCHAR(2),
    address VARCHAR(240),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT stores_status_check CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_stores_company_id ON stores(company_id);

INSERT INTO companies (id, name, document, email, phone, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'Empresa Padrao EAI',
    '00000000000191',
    'admin@eai.com',
    NULL,
    'ACTIVE',
    NOW(),
    NOW()
);

INSERT INTO stores (id, company_id, name, document, email, phone, city, state, address, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    '00000000-0000-0000-0000-000000000101',
    'Loja Padrao EAI',
    '00000000000192',
    'admin@eai.com',
    NULL,
    'Sao Paulo',
    'SP',
    NULL,
    'ACTIVE',
    NOW(),
    NOW()
);

ALTER TABLE users ADD COLUMN company_id UUID;
ALTER TABLE users ADD COLUMN store_id UUID;

UPDATE users
SET company_id = '00000000-0000-0000-0000-000000000101',
    store_id = '00000000-0000-0000-0000-000000000201'
WHERE id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE users ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE users ALTER COLUMN store_id SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_company_id FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE users
    ADD CONSTRAINT fk_users_store_id FOREIGN KEY (store_id) REFERENCES stores(id);

CREATE INDEX idx_users_company_id ON users(company_id);
CREATE INDEX idx_users_store_id ON users(store_id);
