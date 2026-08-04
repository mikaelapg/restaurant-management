-- =============================================================
-- V1 — Criação das tabelas do sistema de gestão de restaurantes
-- Estratégia de herança: JOINED (tabela base + tabelas filhas)
-- =============================================================

-- Tabela base: armazena campos comuns a todos os usuários
CREATE TABLE users (
    id               BIGSERIAL       PRIMARY KEY,
    user_type        VARCHAR(31)     NOT NULL,                   -- discriminador da herança
    name             VARCHAR(255)    NOT NULL,
    email            VARCHAR(255)    NOT NULL UNIQUE,
    login            VARCHAR(100)    NOT NULL UNIQUE,
    password         VARCHAR(255)    NOT NULL,
    last_modified_at TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Endereço embutido (@Embedded Address)
    street           VARCHAR(255),
    number           VARCHAR(50),
    complement       VARCHAR(255),
    city             VARCHAR(100),
    state            VARCHAR(100),
    zip_code         VARCHAR(20),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_login UNIQUE (login)
);

-- Índice para busca por nome (case-insensitive)
CREATE INDEX idx_users_name ON users (LOWER(name));

-- =============================================================
-- Tabela: donos de restaurante
-- =============================================================
CREATE TABLE restaurant_owners (
    id              BIGINT          PRIMARY KEY,
    restaurant_name VARCHAR(255),
    cnpj            VARCHAR(18),

    CONSTRAINT fk_restaurant_owners_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_restaurant_owners_cnpj UNIQUE (cnpj)
);

-- =============================================================
-- Tabela: clientes
-- =============================================================
CREATE TABLE customers (
    id              BIGINT          PRIMARY KEY,
    cpf             VARCHAR(14),
    date_of_birth   DATE,

    CONSTRAINT fk_customers_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_customers_cpf UNIQUE (cpf)
);
