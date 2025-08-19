-- Insert 3 tickets for the banking application
-- Execute this in your H2 database

-- DROP TABLE IF EXISTS tickets;

INSERT INTO tickets (title, description, status, solution) 
VALUES (
    'Mobile app login failed',
    'Customer cannot log into the mobile application since this morning. Error message: "Invalid credentials" even though the credentials work fine on the website.',
    'OPEN',
    NULL
);

INSERT INTO tickets (title, description, status, solution) 
VALUES (
    'Wire transfer blocked by fraud',
    'Wire transfer of €15,000 automatically blocked by the anti-fraud system. Customer wants to make this transfer to buy a car. Supporting documents have been provided.',
    'IN_PROGRESS',
    NULL
);

INSERT INTO tickets (title, description, status, solution) 
VALUES (
    'Card swallowed by ATM Nantes',
    'Customer''s bank card was swallowed by the ATM at Nantes Centre branch. Customer was making a €200 withdrawal. Card needs to be retrieved and new card ordered.',
    'CLOSED',
    'Card retrieved from ATM provider. New card ordered and shipped to customer''s address. Expected delivery: 3-5 business days. Previous PIN reactivated on new card.'
);