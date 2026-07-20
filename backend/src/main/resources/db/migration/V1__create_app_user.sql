CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    birth_date DATE NOT NULL,
    education_level VARCHAR(50) NOT NULL,
    profile VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uk_app_user_keycloak_id ON app_user (keycloak_id);
CREATE UNIQUE INDEX uk_app_user_email_active ON app_user (email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_app_user_cpf_active ON app_user (cpf) WHERE deleted_at IS NULL;
