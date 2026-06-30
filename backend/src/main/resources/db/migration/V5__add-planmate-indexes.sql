CREATE INDEX IF NOT EXISTS idx_expenses_group_id        ON planmate.expenses (group_id);
CREATE INDEX IF NOT EXISTS idx_expenses_paid_by_user_id ON planmate.expenses (paid_by_user_id);
CREATE INDEX IF NOT EXISTS idx_members_group_id         ON planmate.members (group_id);
CREATE INDEX IF NOT EXISTS idx_ep_user_id               ON planmate.expense_participants (user_id);
CREATE INDEX IF NOT EXISTS idx_settlements_group_id     ON planmate.settlements (group_id);
CREATE INDEX IF NOT EXISTS idx_settlements_from_user_id ON planmate.settlements (from_user_id);
CREATE INDEX IF NOT EXISTS idx_settlements_to_user_id   ON planmate.settlements (to_user_id);

-- keycloak_id: unique index doubles as the lookup index for findByKeycloakId
-- and enforces a single Keycloak identity per app user.
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_keycloak_id  ON planmate.users (keycloak_id);
