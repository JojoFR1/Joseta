CREATE TABLE IF NOT EXISTS guilds (
    id BIGINT             PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    icon_url              VARCHAR(255),
    owner_id              BIGINT NOT NULL,
    last_sanction_number  INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS configurations (
    guild_id                        BIGINT PRIMARY KEY,

    welcome_enabled                 BOOLEAN NOT NULL DEFAULT FALSE,
    welcome_image_enabled           BOOLEAN NOT NULL DEFAULT FALSE,
    welcome_channel_id              BIGINT,
    welcome_join_message            TEXT DEFAULT 'Bienvenue {{user}} !',
    welcome_leave_message           TEXT DEFAULT '**{{userName}}** nous a quitté...',
    join_role_id                    BIGINT,
    join_role_bot_id                BIGINT,
    role_verified_id                BIGINT,

    markov_enabled                  BOOLEAN NOT NULL DEFAULT FALSE,

    moderation_enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    moderation_honeypot_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_honeypot_channel_id  BIGINT,
    rules                           TEXT DEFAULT '',

    auto_response_enabled           BOOLEAN NOT NULL DEFAULT FALSE,

    counting_enabled                BOOLEAN NOT NULL DEFAULT FALSE,
    counting_comments_enabled       BOOLEAN NOT NULL DEFAULT FALSE,
    counting_penalty_enabled        BOOLEAN NOT NULL DEFAULT FALSE,
    counting_channel_id             BIGINT,

    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);


CREATE TYPE ENTITY_TYPE AS ENUM ('USER', 'ROLE', 'CHANNEL');

CREATE TABLE IF NOT EXISTS markov_blacklist (
    guild_id BIGINT NOT NULL,
    entity_id BIGINT NOT NULL,

    type ENTITY_TYPE NOT NULL,

    PRIMARY KEY (guild_id, entity_id),
    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT NOT NULL,
    guild_id        BIGINT NOT NULL,

    name            VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(255),
    creation_date   TIMESTAMPTZ NOT NULL,
    sanction_count  INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id, guild_id),
    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sanctions (
    guild_id         BIGINT NOT NULL,
    sanction_number  INT NOT NULL ,

    type             CHAR NOT NULL,
    user_id          BIGINT NOT NULL,
    moderator_id     BIGINT NOT NULL,
    reason           TEXT DEFAULT 'Aucun motif fourni.',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at       TIMESTAMPTZ,
    is_expired       BOOLEAN NOT NULL DEFAULT FALSE,
    is_permanent     BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (guild_id, sanction_number),
    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id, guild_id) REFERENCES users(id, guild_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reminders (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    guild_id      BIGINT NOT NULL,
    channel_id    BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,

    text          TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remind_at     TIMESTAMPTZ NOT NULL,
    repeat_after  BIGINT,
    dm            BOOLEAN NOT NULL DEFAULT FALSE,
    repeat        BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT PRIMARY KEY,

    guild_id        BIGINT NOT NULL,
    channel_id      BIGINT NOT NULL,
    author_id       BIGINT NOT NULL,
    content         TEXT NOT NULL,
    markov_content  TEXT DEFAULT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);
