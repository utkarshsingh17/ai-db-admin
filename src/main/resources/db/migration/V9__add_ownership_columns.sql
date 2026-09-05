-- Each monitored database now belongs to exactly one admin (whoever registered it); each admin
-- account records which admin created it (NULL for the very first, self-registered bootstrap admin).
-- Visibility: an admin sees only databases they personally registered; a viewer sees whatever
-- databases the admin who created their account registered.

ALTER TABLE admin_user
    ADD COLUMN created_by_admin_id UUID REFERENCES admin_user(id);

ALTER TABLE monitored_database
    ADD COLUMN owner_admin_id UUID REFERENCES admin_user(id);

-- Backfill: every already-registered database and every already-existing viewer account is
-- assigned to the earliest-created DB_ADMIN, so nothing already in the system silently disappears
-- from view. No-op on a fresh install (both tables empty at migration time).
UPDATE monitored_database
SET owner_admin_id = (SELECT id FROM admin_user WHERE role = 'DB_ADMIN' ORDER BY created_at ASC LIMIT 1)
WHERE owner_admin_id IS NULL;

UPDATE admin_user
SET created_by_admin_id = (SELECT id FROM admin_user WHERE role = 'DB_ADMIN' ORDER BY created_at ASC LIMIT 1)
WHERE role = 'DB_VIEWER' AND created_by_admin_id IS NULL;

ALTER TABLE monitored_database ALTER COLUMN owner_admin_id SET NOT NULL;

CREATE INDEX idx_monitored_database_owner ON monitored_database(owner_admin_id);
CREATE INDEX idx_admin_user_created_by ON admin_user(created_by_admin_id);
