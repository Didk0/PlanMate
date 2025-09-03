ALTER TABLE planmate.users
    RENAME COLUMN name TO username;

ALTER TABLE planmate.users
    ADD COLUMN keycloak_id UUID NOT NULL,
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255);

ALTER TABLE planmate.users
    ADD CONSTRAINT uq_username UNIQUE (username);

ALTER TABLE planmate.users
    ADD CONSTRAINT uq_email UNIQUE (email);