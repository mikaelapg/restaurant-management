-- =============================================================
-- V2 — Tabela de tipos de usuário (Fase 2)
-- Por enquanto só o cadastro. A associação com users (FK + seed de
-- "Dono de Restaurante" e "Cliente") entra numa migration seguinte.
-- =============================================================

CREATE TABLE user_types (
    id      BIGSERIAL      PRIMARY KEY,
    name    VARCHAR(50)    NOT NULL
);

-- Unicidade sem diferenciar maiúsculas: "Cliente" e "cliente" não podem coexistir.
-- Um UNIQUE comum na coluna aceitaria os dois; o índice sobre LOWER(name) não aceita.
CREATE UNIQUE INDEX uk_user_types_name_lower ON user_types (LOWER(name));
