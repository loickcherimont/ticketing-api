-- 1. le user "système" qui adoptera les tickets existants
INSERT INTO users (id, email, password, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'system@ticketing.internal', '$2y$10$wdArBaWyDCs48jEKHkqJEe9VHe4NMyr8xx.nlAYQxw9XxeHmk9edu', 'USER');

-- 2. ajout des colonnes D'ABORD nullable
ALTER TABLE tickets ADD COLUMN created_by_id uuid;
ALTER TABLE tickets ADD COLUMN assigned_to_id uuid;

-- 3. rattacher tous les tickets existants au user système
UPDATE tickets SET created_by_id = '00000000-0000-0000-0000-000000000001' WHERE created_by_id IS NULL;

-- 4. MAINTENANT seulement : NOT NULL (aucune donnée orpheline, donc aucun risque)
ALTER TABLE tickets ALTER COLUMN created_by_id SET NOT NULL;

-- 5. les clés étrangères
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by_id) REFERENCES users (id);
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id);