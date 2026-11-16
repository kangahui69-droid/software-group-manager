-- AUD-02: run once on MySQL 8 after backing up and before deploying the new WAR.
-- Nullable columns preserve historical rows and keep the previous WAR insert compatible.
-- Appending columns allows MySQL 8 InnoDB's instant ADD COLUMN where supported.
ALTER TABLE `operation_log`
  ADD COLUMN `result` VARCHAR(16) NULL,
  ADD COLUMN `http_status` SMALLINT UNSIGNED NULL,
  ADD COLUMN `duration_ms` BIGINT UNSIGNED NULL,
  ADD COLUMN `request_id` CHAR(36) NULL,
  ADD COLUMN `error_code` VARCHAR(64) NULL,
  ADD COLUMN `target_type` VARCHAR(32) NULL,
  ADD COLUMN `target_id` VARCHAR(64) NULL,
  ALGORITHM=INSTANT;
