DELETE FROM planmate.settlements;

ALTER TABLE planmate.settlements
    ADD CONSTRAINT uq_settlements_group_from_to UNIQUE (group_id, from_user_id, to_user_id);
