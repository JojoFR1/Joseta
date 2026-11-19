CREATE TABLE event_history (
    guild_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    id BIGINT NOT NULL, -- -1 unknown, 0 restricted, 1 timeout, 2 unverified, 3 8ball, otherwise the id of the role that was given

    count INT NOT NULL DEFAULT 1,
    time BIGINT NOT NULL,

    PRIMARY KEY (guild_id, user_id, id)
)
