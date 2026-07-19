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
    MessageEntity getById(long id);
    
    @SqlQuery("SELECT * FROM messages WHERE guild_id = :guildId")
    @RegisterFieldMapper(value = MessageEntity.class)
    Stream<MessageEntity> getByGuildId(long guildId);
    
    @SqlUpdate("UPDATE messages SET markov_content = NULL WHERE author_id = :authorId AND guild_id = :guildId")
    void clearMarkovContent(long authorId, long guildId);
    
    @SqlUpdate("DELETE FROM messages WHERE id = :id")
    void delete(long id);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId AND author_id = :authordId")
    void deleteByAuthorId(long guildId, long authordId);
    @SqlUpdate("DELETE FROM messages WHERE channel_id = :channelId")
    void deleteByChannelId(long channelId);
    @SqlUpdate("DELETE FROM messages WHERE guild_id = :guildId")
    void deleteByGuildId(long guildId);
    
    interface MarkovBlacklistDao {
        @SqlUpdate("""
            INSERT INTO markov_blacklist (guild_id, entity_id, type)
            VALUES (:guildId, :entityId, CAST(:type AS ENTITY_TYPE))
            ON CONFLICT (guild_id, entity_id) DO NOTHING
        """)
        void add(long guildId, EntityType type, long entityId);
        @SqlBatch("""
            INSERT INTO markov_blacklist (guild_id, entity_id, type)
            VALUES (:guildId, :entityIds, CAST(:type AS ENTITY_TYPE))
            ON CONFLICT (guild_id, entity_id) DO NOTHING
        """)
        void addAll(long guildId, EntityType type, Iterable<Long> entityIds);
        
        @SqlQuery("SELECT entity_id FROM markov_blacklist WHERE guild_id = :guildId")
        Set<Long> getAllIds(long guildId);
        @SqlQuery("SELECT entity_id FROM markov_blacklist WHERE guild_id = :guildId AND type = CAST(:type AS ENTITY_TYPE)")
        Set<Long> getIds(long guildId, EntityType type);
        
        @SqlQuery("SELECT EXISTS (SELECT 1 FROM markov_blacklist WHERE guild_id = :guildId AND entity_id = :entityId)")
        boolean isIdBlacklisted(@Bind("guildId") long guildId, @Bind("entityId") long entityId);
        @SqlQuery("SELECT EXISTS (SELECT 1 FROM markov_blacklist WHERE guild_id = :guildId AND entity_id IN (<entityIds>))")
        boolean isAnyIdBlacklisted(@Bind("guildId") long guildId, @BindList("entityIds") Iterable<Long> entityIds);
        
        @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id = :guildId AND entity_id = :entityId")
        void remove(long guildId, long entityId);
        @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id = :guildId AND entity_id IN (<entityIds>) AND type = CAST(:type AS ENTITY_TYPE)")
        void removeAll(long guildId, EntityType type, @BindList("entityIds") Iterable<Long> entityIds);
        @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id := :guildId AND type = CAST(:type AS ENTITY_TYPE);")
        void clearByType(long guildId, EntityType type);
    }
    
    enum EntityType { USER, ROLE, CHANNEL }
}
