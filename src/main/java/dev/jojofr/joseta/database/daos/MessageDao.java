package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.MessageEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.stream.Stream;

public interface MessageDao {
    @SqlUpdate("""
        INSERT INTO messages (id, guild_id, channel_id, author_id, content, markov_content, created_at)
        VALUES (:id, :guildId, :channelId, :authorId, :content, :markovContent, :createdAt)
        ON CONFLICT (id) DO UPDATE SET
            guild_id = EXCLUDED.guild_id,
            channel_id = EXCLUDED.channel_id,
            author_id = EXCLUDED.author_id,
            content = EXCLUDED.content,
            markov_content = EXCLUDED.markov_content,
            created_at = EXCLUDED.created_at
    """)
    void upsert(@BindFields MessageEntity message);
    
    @SqlQuery("SELECT * FROM messages WHERE id = :id")
    @RegisterFieldMapper(value = MessageEntity.class)
    MessageEntity getById(@Bind("id") long id);
    
    @SqlQuery("SELECT * FROM messages WHERE guild_id = :guildId")
    @RegisterFieldMapper(value = MessageEntity.class)
    Stream<MessageEntity> getByGuildId(@Bind("guildId") long guildId);
    
    @SqlUpdate("UPDATE messages SET markov_content = NULL WHERE author_id = :authorId AND guild_id = :guildId")
    void clearMarkovContent(@Bind("authorId") long authorId, @Bind("guildId") long guildId);
    
    @SqlUpdate("DELETE FROM messages WHERE id = :id")
    void delete(@Bind("id") long id);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId AND author_id = :authordId")
    void deleteByAuthorId(@Bind("guildId") long guildId, @Bind("authordId") long authordId);
    @SqlUpdate("DELETE FROM messages WHERE channel_id = :channelId")
    void deleteByChannelId(@Bind("channelId") long channelId);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId")
    void deleteByGuildId(@Bind("guildId") long guildId);
}
