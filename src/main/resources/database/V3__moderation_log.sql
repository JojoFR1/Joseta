ALTER TABLE configurations
    ADD COLUMN IF NOT EXISTS moderation_log_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS moderation_log_channel_id BIGINT;

CREATE TABLE IF NOT EXISTS message_attachments (
    id            BIGINT PRIMARY KEY,

    message_id    BIGINT NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    extension     VARCHAR(10),
    description   TEXT,
    content_type  VARCHAR(30),
    url           TEXT NOT NULL,
    proxy_url     TEXT NOT NULL,
    size          INT NOT NULL,
    width         INT NOT NULL,
    height        INT NOT NULL,

    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
)