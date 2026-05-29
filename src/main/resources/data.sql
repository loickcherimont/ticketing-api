-- FAKE DATA for PostgreSQL:
-- Insert 3 tickets for the banking application

INSERT INTO 
    tickets (title, description, status, solution) 
VALUES
(
  'Échec de connexion à l''application mobile',
  'Le client ne peut plus se connecter à l''application mobile depuis ce matin. Message d''erreur : "Identifiants invalides", alors que les identifiants fonctionnent sur le site web.',
  'OPEN',
  NULL
),
(
  'Virement bloqué par le système antifraude',
  'Un virement de 15 000 € a été automatiquement bloqué par le système antifraude. Le client souhaite effectuer ce transfert pour acheter une voiture. Les justificatifs ont été fournis.',
  'IN_PROGRESS',
  NULL
),
(
  'Carte avalée par un distributeur à Nantes',
  'La carte bancaire du client a été avalée par le distributeur automatique de billets de l''agence Nantes Centre. Le client retirait 200 €. La carte doit être récupérée et une nouvelle carte commandée.',
  'CLOSED',
  'Carte récupérée auprès de l''opérateur du DAB. Nouvelle carte commandée et envoyée à l''adresse du client. Délai de livraison estimé : 3 à 5 jours ouvrés. Le code PIN précédent a été réactivé sur la nouvelle carte.'
);

-- FAKE DATA for PostgreSQL:
-- Insert 1 user and 1 agent for the banking application
INSERT INTO
  users(email, password, role)
VALUES
    ('john.doe@gmail.com', '$2a$10$ZCkYIwrSw8t2mi3klTFKeu9y.jjWABOnWPwbZUlkwTVzc1oQtjGou', 'USER'),
    ('agent@company.com', '$2a$10$xtGnHBCYKz3KvMRQmt4fmOplvodVpPxSaE4vL9ijLn4RHFwxZWRBu', 'AGENT');