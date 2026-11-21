-- DATA-02: run against a backed-up production database before deploying Argon2id code.
-- Execute once in a controlled maintenance window; this script does not change password values.
ALTER TABLE `user` MODIFY COLUMN `password` VARCHAR(255) NOT NULL;
