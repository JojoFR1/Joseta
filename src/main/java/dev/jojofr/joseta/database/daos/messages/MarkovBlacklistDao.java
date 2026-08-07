package dev.jojofr.joseta.database.daos.messages;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

public interface MarkovBlacklistDao {
    enum EntityType { USER, ROLE, CHANNEL }
    
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
    @SqlUpdate("DELETE FROM markov_blacklist WHERE guild_id = :guildId AND type = CAST(:type AS ENTITY_TYPE);")
    void clearByType(long guildId, EntityType type);
}