package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.MessageEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.customizer.BindList;
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
    
    interface MarkovBlacklistDao {
        @SqlUpdate("""
            INSERT INTO markov_blacklist (guild_id, entity_id, type)
            VALUES (:guildId, :entityId, CAST(:type AS ENTITY_TYPE))
            ON CONFLICT (guild_id, entity_id) DO NOTHING
        """)
        void add(@Bind("guildId") long guildId, @Bind("type") EntityType type, @Bind("entityId") long entityId);
        @SqlBatch("""
            INSERT INTO markov_blacklist (guild_id, entity_id, type)
            VALUES (:guildId, :entityId, CAST(:type AS ENTITY_TYPE))
            ON CONFLICT (guild_id, entity_id) DO NOTHING
        """)
        void addAll(@Bind("guildId") long guildId, @Bind("type") EntityType type, @Bind("entityId") Iterable<Long> entityIds);
        
        @SqlQuery("SELECT entity_id FROM markov_blacklist WHERE guild_id = :guildId AND type = CAST(:type AS ENTITY_TYPE)")
        Set<Long> getIds(@Bind("guildId") long guildId, @Bind("type") EntityType type);
        
        @SqlQuery("SELECT EXISTS (SELECT 1 FROM markov_blacklist WHERE guild_id = :guildId AND entity_id = :entityId)")
        boolean isIdBlacklisted(@Bind("guildId") long guildId, @Bind("entityId") long entityId);
        @SqlQuery("SELECT EXISTS (SELECT 1 FROM markov_blacklist WHERE guild_id = :guildId AND entity_id IN (<entityIds>))")
        boolean isAnyIdBlacklisted(@Bind("guildId") long guildId, @BindList("entityIds") Iterable<Long> entityIds);
        
        @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id = :guildId AND entity_id = :entityId")
        void remove(@Bind("guildId") long guildId, @Bind("entityId") long entityId);
        @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id = :guildId AND entity_id IN (<entityIds>) AND type = CAST(:type AS ENTITY_TYPE)")
        void removeAll(@Bind("guildId") long guildIds, @Bind("type") EntityType type, @Bind("entityId") Iterable<Long> entityId);
    }
    
    enum EntityType { USER, ROLE, CHANNEL }
}
