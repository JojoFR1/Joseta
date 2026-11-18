CREATE TABLE event_restrictions (
    guild_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (guild_id, user_id)
);
CREATE INDEX idx_event_restrictions_expires_at ON event_restrictions (expires_at);
