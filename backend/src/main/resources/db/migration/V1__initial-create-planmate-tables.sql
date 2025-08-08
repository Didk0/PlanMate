-- Users table
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Groups table
CREATE TABLE groups
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Members table
CREATE TABLE members
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    group_id  BIGINT    NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_group UNIQUE (user_id, group_id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

CREATE TABLE expenses
(
    id              BIGSERIAL PRIMARY KEY,
    description     VARCHAR(255),
    amount          NUMERIC(19, 4) NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    group_id        BIGINT         NOT NULL,
    paid_by_user_id BIGINT         NOT NULL,
    CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_paid_by FOREIGN KEY (paid_by_user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Expense participants table
CREATE TABLE expense_participants
(
    id         BIGSERIAL PRIMARY KEY,
    amount     NUMERIC(19, 4) NOT NULL,
    expense_id BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    CONSTRAINT uq_expense_user UNIQUE (expense_id, user_id),
    CONSTRAINT fk_ep_expense FOREIGN KEY (expense_id) REFERENCES expenses (id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Settlements table
CREATE TABLE settlements
(
    id           BIGSERIAL PRIMARY KEY,
    amount       NUMERIC(19, 4) NOT NULL,
    group_id     BIGINT         NOT NULL,
    from_user_id BIGINT         NOT NULL,
    to_user_id   BIGINT         NOT NULL,
    settled_at   TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT fk_settlements_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_settlements_from_user FOREIGN KEY (from_user_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_settlements_to_user FOREIGN KEY (to_user_id) REFERENCES members (id) ON DELETE CASCADE
);