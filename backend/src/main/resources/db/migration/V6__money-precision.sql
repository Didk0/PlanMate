ALTER TABLE planmate.expenses             ALTER COLUMN amount       TYPE NUMERIC(12, 2);
ALTER TABLE planmate.expense_participants ALTER COLUMN share_amount TYPE NUMERIC(12, 2);
ALTER TABLE planmate.settlements          ALTER COLUMN amount       TYPE NUMERIC(12, 2);
