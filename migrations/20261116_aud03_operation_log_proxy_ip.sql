-- AUD-03: run once after the AUD-02 migration and before deploying the new WAR.
-- Existing rows remain unchanged; proxy_ip is populated only for new proxied requests.
ALTER TABLE `operation_log`
  ADD COLUMN `proxy_ip` VARCHAR(45) NULL,
  ALGORITHM=INSTANT;
