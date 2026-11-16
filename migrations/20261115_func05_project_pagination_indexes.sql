-- FUNC-05: apply once to an existing MySQL database after the normal backup gate.
-- Keep the existing leader_id/admin_id/project_member indexes for user-scoped queries.
-- Contains no data rewrite. MySQL may still choose another index for small tables.
ALTER TABLE `project`
  ADD INDEX `idx_project_deleted_year_created` (`deleted`,`year`,`created_at`,`id`),
  ADD INDEX `idx_project_deleted_status_year_created` (`deleted`,`status`,`year`,`created_at`,`id`),
  ADD INDEX `idx_project_deleted_category_year_created` (`deleted`,`category`,`year`,`created_at`,`id`),
  ALGORITHM=INPLACE, LOCK=NONE;

-- The current keyword predicate is LIKE '%term%'; a normal B-tree index cannot
-- accelerate that pattern. A full-text search change requires separate Chinese
-- tokenization and relevance verification, so it is not part of this migration.
