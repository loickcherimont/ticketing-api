CREATE TABLE users
(
    id       uuid         NOT NULL,
    email    varchar(100) NOT NULL,
    password varchar(255) NOT NULL,
    role     varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE tickets
(
    id          uuid         NOT NULL,
    title       varchar(100) NOT NULL,
    description TEXT         NOT NULL,
    status      varchar(255),
    solution    TEXT,
    PRIMARY KEY (id),
    CONSTRAINT tickets_title_unique UNIQUE (title)
);