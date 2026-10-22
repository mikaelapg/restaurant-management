-- =============================================================
-- V5 — Itens do cardápio (Fase 2)
-- =============================================================

CREATE TABLE menu_items (
    id              BIGSERIAL       PRIMARY KEY,
    restaurant_id   BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500)    NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL,
    dine_in_only    BOOLEAN         NOT NULL DEFAULT FALSE,
    photo_path      VARCHAR(500),

    -- CASCADE: o item não existe sem o restaurante; excluir o restaurante leva o cardápio junto.
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE,
    CONSTRAINT ck_menu_items_price_positive CHECK (price > 0)
);

-- Dentro de um restaurante, dois itens não podem ter o mesmo nome (sem diferenciar maiúsculas).
-- Restaurantes diferentes podem, claro, ter cada um o seu "Risoto".
CREATE UNIQUE INDEX uk_menu_items_restaurant_name_lower ON menu_items (restaurant_id, LOWER(name));
