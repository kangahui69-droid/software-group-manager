CREATE TABLE news_asset (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  news_id INT NULL,
  uploaded_by INT NOT NULL,
  upload_token CHAR(36) NOT NULL,
  kind VARCHAR(16) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(100) NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  file_size BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_news_asset_stored_name (stored_name),
  KEY idx_news_asset_news (news_id, kind, sort_order),
  KEY idx_news_asset_pending (uploaded_by, upload_token, created_at),
  CONSTRAINT fk_news_asset_news FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);
