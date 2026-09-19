package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.database.entities.MessageEntity;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Set;
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
    
    @SqlBatch("""
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
    void upsertBatch(@BindFields List<MessageEntity> messages);
    
    @SqlQuery("SELECT * FROM messages WHERE id = :id")
    @RegisterFieldMapper(value = MessageEntity.class)
    MessageEntity getById(long id);
    
    @SqlQuery("SELECT * FROM messages WHERE guild_id = :guildId")
    @RegisterFieldMapper(value = MessageEntity.class)
    Stream<MessageEntity> getByGuildId(long guildId);
    
    @SqlQuery("""
        SELECT * FROM messages
        WHERE guild_id = :guildId AND channel_id = :channelId AND author_id != :botId AND id != :currentMessageId
        ORDER BY created_at DESC
        LIMIT 1
    """)
    @RegisterFieldMapper(MessageEntity.class)
    MessageEntity getLastCountingMessage(long guildId, long channelId, long botId, long currentMessageId);
    
    @SqlQuery("""
        SELECT * FROM messages
        WHERE guild_id = :guildId AND channel_id = :channelId AND author_id = :botId
            AND content LIKE '%Le mode de comptage spécial a %'
        ORDER BY created_at DESC
        LIMIT 1
    """)
    @RegisterFieldMapper(MessageEntity.class)
    MessageEntity getLastCountingModeChangeMessage(long guildId, long channelId, long botId);
    
    
    @SqlQuery("SELECT COUNT(*) FROM messages WHERE author_id = :authorId AND guild_id = :guildId")
    int getMemberMessageCount(long authorId, long guildId);
    
    @SqlQuery("SELECT COUNT(*) FROM messages WHERE author_id = :authorId AND guild_id = :guildId AND channel_id = :channelId")
    int getMemberChannelMessageCount(long authorId, long guildId, long channelId);
    
    @SqlQuery("""
        WITH ordered AS (
            SELECT author_id, content,
               LEAD(author_id) OVER (ORDER BY id) AS next_author_id,
               LEAD(content) OVER (ORDER BY id) AS next_content
            FROM messages
            WHERE guild_id = :guildId AND channel_id = :channelId
        )
        SELECT COUNT(*) FROM ordered
        WHERE author_id = :authorId AND content ~ :likePattern
            AND NOT (next_author_id = :botId AND next_content ~ CONCAT('<@', :authorId, '> a cassé la chaîne !'))
    """)
    int getMemberCountingMessageCount(long authorId, long guildId, long channelId, long botId, String likePattern);
    
    @SqlQuery("""
        SELECT COUNT(*) FROM messages
        WHERE guild_id = :guildId AND channel_id = :channelId AND author_id = :botId AND content ~ CONCAT('<@', :authorId, '> a cassé la chaîne !')
    """)
    int getMemberChainBreakCount(long authorId, long guildId, long channelId, long botId);
    
    @SqlQuery("SELECT COUNT(*) FROM messages WHERE guild_id = :guildId")
    int getGuildMessageCount(long guildId);
    
    @SqlQuery("SELECT author_id as user_id, COUNT(*) AS count FROM messages WHERE guild_id = :guildId GROUP BY author_id ORDER BY count DESC LIMIT :limit OFFSET :offset")
    @RegisterConstructorMapper(value = LeaderboardEntry.class)
    List<LeaderboardEntry> getMessageLeaderboard(long guildId, int limit, int offset);
    
    @SqlUpdate("UPDATE messages SET markov_content = NULL WHERE author_id = :authorId AND guild_id = :guildId")
    void clearMarkovContent(long authorId, long guildId);
    
    // Set the content, and markov content if not null
    @SqlUpdate("""
        UPDATE messages SET
            content = :content,
            markov_content = CASE
                WHEN :markovContent IS NOT NULL THEN :markovContent
                ELSE NULL
            END
        WHERE id = :id
    """)
    void setContents(long id, String content, String markovContent);
    
    @SqlUpdate("DELETE FROM messages WHERE id = :id")
    void delete(long id);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId AND author_id = :authordId")
    void deleteByAuthorId(long guildId, long authordId);
    @SqlUpdate("DELETE FROM messages WHERE channel_id = :channelId")
    void deleteByChannelId(long channelId);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId")
    void deleteByGuildId(long guildId);
}
