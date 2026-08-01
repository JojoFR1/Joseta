CREATE INDEX IF NOT EXISTS idx_messages_guild_author_id ON messages(guild_id, author_id);
