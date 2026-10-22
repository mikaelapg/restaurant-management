-- =============================================================
-- V6 — Tipos de sistema ligados aos perfis de usuário (Fase 2)
--
-- "Dono de Restaurante" e "Cliente" passam a ser tipos de sistema: cada um fica
-- ligado a um perfil (o discriminador users.user_type). É essa ligação, e não o
-- nome, que diz quem pode ser dono de restaurante; por isso eles podem ser
-- renomeados, mas não excluídos.
-- =============================================================

ALTER TABLE user_types ADD COLUMN profile VARCHAR(30);
ALTER TABLE user_types ADD CONSTRAINT uk_user_types_profile UNIQUE (profile);
ALTER TABLE user_types ADD CONSTRAINT ck_user_types_profile CHECK (profile IN ('RESTAURANT_OWNER', 'CUSTOMER'));

-- Os tipos que a V3 cadastrou viram tipos de sistema.
UPDATE user_types SET profile = 'RESTAURANT_OWNER' WHERE LOWER(name) = LOWER('Dono de Restaurante');
UPDATE user_types SET profile = 'CUSTOMER'         WHERE LOWER(name) = LOWER('Cliente');

-- Se algum deles tiver sido renomeado ou excluído antes desta migration, é recriado.
-- O sufixo evita colidir com o índice único de nome caso o nome já esteja em uso.
INSERT INTO user_types (name, profile)
SELECT CASE WHEN EXISTS (SELECT 1 FROM user_types WHERE LOWER(name) = LOWER('Dono de Restaurante'))
            THEN 'Dono de Restaurante (sistema)' ELSE 'Dono de Restaurante' END,
       'RESTAURANT_OWNER'
WHERE NOT EXISTS (SELECT 1 FROM user_types WHERE profile = 'RESTAURANT_OWNER');

INSERT INTO user_types (name, profile)
SELECT CASE WHEN EXISTS (SELECT 1 FROM user_types WHERE LOWER(name) = LOWER('Cliente'))
            THEN 'Cliente (sistema)' ELSE 'Cliente' END,
       'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM user_types WHERE profile = 'CUSTOMER');

-- Usuários sem tipo recebem o tipo de sistema do seu perfil.
UPDATE users u
SET user_type_id = t.id
FROM user_types t
WHERE u.user_type_id IS NULL
  AND t.profile = u.user_type;

-- Usuários com o tipo de sistema do outro perfil (ex.: dono com "Cliente") voltam ao do
-- próprio perfil: a partir daqui o domínio recusa essa combinação.
UPDATE users u
SET user_type_id = own.id
FROM user_types cur, user_types own
WHERE cur.id = u.user_type_id
  AND cur.profile IS NOT NULL
  AND cur.profile <> u.user_type
  AND own.profile = u.user_type;

-- O endereço do usuário continua opcional, mas, quando existe, precisa estar completo,
-- como o dos restaurantes. Endereços parciais gravados na Fase 1 são descartados.
UPDATE users
SET street = NULL, number = NULL, complement = NULL, city = NULL, state = NULL, zip_code = NULL
WHERE COALESCE(TRIM(street), '') = ''
   OR COALESCE(TRIM(number), '') = ''
   OR COALESCE(TRIM(city), '') = ''
   OR COALESCE(TRIM(state), '') = ''
   OR COALESCE(TRIM(zip_code), '') = ''
   OR LENGTH(number) > 20
   OR LENGTH(state) > 50;
