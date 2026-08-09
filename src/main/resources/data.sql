-- FAKE DATA for PostgreSQL:
-- Insert 3 tickets for the banking application

INSERT INTO 
    tickets (id, title, description, status, solution) 
VALUES
(
  'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',
  'Échec de connexion à l''application mobile',
  'Le client ne peut plus se connecter à l''application mobile depuis ce matin. Message d''erreur : "Identifiants invalides", alors que les identifiants fonctionnent sur le site web.',
  'OPEN',
  NULL
),
(
  'b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e',
  'Virement bloqué par le système antifraude',
  'Un virement de 15 000 € a été automatiquement bloqué par le système antifraude. Le client souhaite effectuer ce transfert pour acheter une voiture. Les justificatifs ont été fournis.',
  'IN_PROGRESS',
  NULL
),
(
  'c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f',
  'Carte avalée par un distributeur à Nantes',
  'La carte bancaire du client a été avalée par le distributeur automatique de billets de l''agence Nantes Centre. Le client retirait 200 €. La carte doit être récupérée et une nouvelle carte commandée.',
  'CLOSED',
  'Carte récupérée auprès de l''opérateur du DAB. Nouvelle carte commandée et envoyée à l''adresse du client. Délai de livraison estimé : 3 à 5 jours ouvrés. Le code PIN précédent a été réactivé sur la nouvelle carte.'
);

-- FAKE DATA for PostgreSQL:
-- Insert 1 user and 1 agent for the banking application
INSERT INTO
  users(id, email, password, role)
VALUES
    ('d4e5f6a7-b8c9-4d0e-1f2a-3b4c5d6e7f80', 'john.doe@gmail.com', '$2a$10$ZCkYIwrSw8t2mi3klTFKeu9y.jjWABOnWPwbZUlkwTVzc1oQtjGou', 'USER'),
    ('e5f6a7b8-c9d0-4e1f-2a3b-4c5d6e7f8091', 'agent@company.com', '$2a$10$xtGnHBCYKz3KvMRQmt4fmOplvodVpPxSaE4vL9ijLn4RHFwxZWRBu', 'AGENT');