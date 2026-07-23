ALTER TABLE planmate.members
    ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'MEMBER';

WITH first_members AS (SELECT DISTINCT ON (group_id) id
                        FROM planmate.members
                        ORDER BY group_id, joined_at, id)
UPDATE planmate.members m
SET role = 'OWNER'
FROM first_members fm
WHERE m.id = fm.id;

ALTER TABLE planmate.members
    ADD CONSTRAINT ck_members_role CHECK (role IN ('OWNER', 'MEMBER'));
