-- Run manually against an existing production database after a verified backup.
-- The duplicate query must return zero rows before applying the unique index.
SELECT session_id, COUNT(*) AS copies
FROM ai_conversation
GROUP BY session_id
HAVING COUNT(*) > 1;

ALTER TABLE ai_conversation
  ADD UNIQUE KEY uk_ai_conversation_session_id (session_id);
