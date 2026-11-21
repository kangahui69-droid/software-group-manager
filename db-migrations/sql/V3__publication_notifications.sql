CREATE TABLE notification_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_type VARCHAR(16) NOT NULL,
    source_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active TINYINT NOT NULL DEFAULT 1,
    KEY idx_notification_source (source_type, source_id, active),
    KEY idx_notification_created (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_recipient (
    notification_id BIGINT NOT NULL,
    user_id INT NOT NULL,
    read_at DATETIME DEFAULT NULL,
    PRIMARY KEY (notification_id, user_id),
    KEY idx_notification_user_read (user_id, read_at, notification_id),
    CONSTRAINT fk_notification_recipient_event FOREIGN KEY (notification_id)
        REFERENCES notification_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_recipient_user FOREIGN KEY (user_id)
        REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
