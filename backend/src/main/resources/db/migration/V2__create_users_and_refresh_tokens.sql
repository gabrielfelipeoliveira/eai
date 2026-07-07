CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    phone VARCHAR(40),
    job_title VARCHAR(120),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT user_roles_role_check CHECK (role IN ('ADMIN', 'MANAGER', 'SELLER', 'RECEPTIONIST', 'AUDITOR'))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

INSERT INTO users (id, name, email, password_hash, phone, job_title, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrador EAI',
    'admin@eai.com',
    '$2a$10$rittMsrhSOzttZy/ST1Pz.AEC1YrNnxA7MaGVS2JApfL871P.yn/6',
    NULL,
    'Administrador',
    'ACTIVE',
    NOW(),
    NOW()
);

INSERT INTO user_roles (user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'ADMIN');
