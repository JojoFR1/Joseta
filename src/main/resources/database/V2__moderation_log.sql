ALTER TABLE configurations
    ADD COLUMN moderation_log_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN moderation_log_channel_id BIGINT;
