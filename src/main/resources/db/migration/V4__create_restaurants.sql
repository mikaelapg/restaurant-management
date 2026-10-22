-- =============================================================
-- V4 — Restaurantes (Fase 2)
-- =============================================================

CREATE TABLE restaurants (
    id            BIGSERIAL       PRIMARY KEY,
    name          VARCHAR(100)    NOT NULL,
    cuisine_type  VARCHAR(50)     NOT NULL,
    opens_at      TIME            NOT NULL,
    closes_at     TIME            NOT NULL,
    owner_id      BIGINT          NOT NULL,

    -- Endereço embutido, nos mesmos moldes da tabela users
    street        VARCHAR(255)    NOT NULL,
    number        VARCHAR(20)     NOT NULL,
    complement    VARCHAR(255),
    city          VARCHAR(100)    NOT NULL,
    state         VARCHAR(50)     NOT NULL,
    zip_code      VARCHAR(20)     NOT NULL,

    -- RESTRICT: excluir um usuário que é dono de restaurante é recusado pelo banco.
    -- Os restaurantes precisam ser excluídos ou passados a outro dono antes.
    CONSTRAINT fk_restaurants_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT,

    -- Mesma regra do OpeningHours: abertura e fechamento iguais não têm significado.
    -- Fechamento antes da abertura é permitido (o restaurante vira a noite).
    CONSTRAINT ck_restaurants_opening_hours CHECK (opens_at <> closes_at)
);

CREATE INDEX idx_restaurants_owner_id ON restaurants (owner_id);
CREATE INDEX idx_restaurants_name ON restaurants (LOWER(name));
