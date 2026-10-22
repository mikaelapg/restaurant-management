-- =============================================================
-- V3 — Associação entre usuários e tipos de usuário (Fase 2)
-- =============================================================

-- Os dois tipos que o enunciado pede. Só entram se ainda não existirem, para não
-- colidir com um tipo de mesmo nome cadastrado pela API antes desta migration.
INSERT INTO user_types (name)
SELECT 'Dono de Restaurante'
WHERE NOT EXISTS (SELECT 1 FROM user_types WHERE LOWER(name) = LOWER('Dono de Restaurante'));

INSERT INTO user_types (name)
SELECT 'Cliente'
WHERE NOT EXISTS (SELECT 1 FROM user_types WHERE LOWER(name) = LOWER('Cliente'));

-- Cada usuário aponta para um tipo. A coluna é anulável porque o tipo é um cadastro
-- gerenciado pela API: se alguém renomear "Cliente", um cadastro novo fica sem tipo
-- até ser associado, em vez de falhar.
-- ON DELETE RESTRICT: o banco recusa excluir um tipo que algum usuário ainda usa.
ALTER TABLE users
    ADD COLUMN user_type_id BIGINT,
    ADD CONSTRAINT fk_users_user_type
        FOREIGN KEY (user_type_id) REFERENCES user_types (id) ON DELETE RESTRICT;

CREATE INDEX idx_users_user_type_id ON users (user_type_id);

-- Usuários que já existiam recebem o tipo correspondente ao seu perfil.
UPDATE users u
SET user_type_id = t.id
FROM user_types t
WHERE u.user_type_id IS NULL
  AND (   (u.user_type = 'RESTAURANT_OWNER' AND LOWER(t.name) = LOWER('Dono de Restaurante'))
       OR (u.user_type = 'CUSTOMER'         AND LOWER(t.name) = LOWER('Cliente')));
