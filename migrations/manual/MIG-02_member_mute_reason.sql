-- Apply only after a verified database backup and after checking that the column is absent.
-- This migration is required before deploying the member-mute implementation.
SELECT COUNT(*) AS existing_column
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'group_member' AND COLUMN_NAME = 'mute_reason';

ALTER TABLE group_member
  ADD COLUMN mute_reason VARCHAR(255) NULL AFTER muted_until;
