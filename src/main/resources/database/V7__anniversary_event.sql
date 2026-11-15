CREATE TABLE event_temporary_roles (
    guild_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (guild_id, user_id, role_id)
);
CREATE INDEX idx_event_temporary_role_expires_at ON event_temporary_roles (expires_at);

CREATE TABLE event_gacha_users (
    user_id BIGINT PRIMARY KEY,
    last_pull_at TIMESTAMPTZ NOT NULL,
    pulls INT NOT NULL DEFAULT 0
);

CREATE TABLE event_gacha_cards (
    user_id BIGINT NOT NULL,
    card_id INT NOT NULL,
    copies INT NOT NULL DEFAULT 1,
    first_obtained_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, card_id)
)
