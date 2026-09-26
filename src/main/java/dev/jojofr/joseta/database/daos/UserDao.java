package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.database.entities.UserEntity;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface UserDao {
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, sanction_count, time_voice, counting_success, counting_fail, counting_special_success, counting_special_fail)
        VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, :sanctionCount, :timeVoice, :countingSuccess, :countingFail, :countingSpecialSuccess, :countingSpecialFail)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date,
            sanction_count = EXCLUDED.sanction_count,
            time_voice = EXCLUDED.time_voice,
            counting_success = EXCLUDED.counting_success,
            counting_fail = EXCLUDED.counting_fail,
            counting_special_success = EXCLUDED.counting_special_success,
            counting_special_fail = EXCLUDED.counting_special_fail
    """)
    void upsert(@BindFields UserEntity user);
    
    @SqlUpdate("UPDATE users SET sanction_count = sanction_count + 1 WHERE id = :id AND guild_id = :guildId")
    void incrementSanctionCount(long id, long guildId);
    
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, time_voice)
            VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, :timeVoice)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            time_voice = users.time_voice + EXCLUDED.time_voice,
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date
    """)
    void addTimeVoice(@BindFields UserEntity user);
    
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, counting_success)
            VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, 1)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            counting_success = users.counting_success + 1,
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date
    """)
    void incrementCountingSuccess(@BindFields UserEntity user);
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, counting_fail)
            VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, 1)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            counting_fail = users.counting_fail + 1,
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date
    """)
    void incrementCountingFail(@BindFields UserEntity user);
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, counting_special_success)
            VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, 1)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            counting_special_success = users.counting_special_success + 1,
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date
    """)
    void incrementCountingSpecialSuccess(@BindFields UserEntity user);
    @SqlUpdate("""
        INSERT INTO users (id, guild_id, name, avatar_url, creation_date, counting_special_fail)
            VALUES (:id, :guildId, :name, :avatarUrl, :creationDate, 1)
        ON CONFLICT (id, guild_id) DO UPDATE SET
            counting_special_fail = users.counting_special_fail + 1,
            name = EXCLUDED.name,
            avatar_url = EXCLUDED.avatar_url,
            creation_date = EXCLUDED.creation_date
    """)
    void incrementCountingSpecialFail(@BindFields UserEntity user);
    
    
    @SqlQuery("SELECT id, time_voice as count FROM users WHERE guild_id = :guildId AND time_voice > 0 ORDER BY time_voice DESC LIMIT :limit OFFSET :offset")
    @RegisterConstructorMapper(value = LeaderboardEntry.class)
    List<LeaderboardEntry> getVoiceLeaderboard(long guildId, int limit, int offset);
    
    @SqlQuery("SELECT * FROM users WHERE id = :id AND guild_id = :guildId")
    @RegisterFieldMapper(value = UserEntity.class)
    UserEntity getById(long id, long guildId);
    
    @SqlQuery("SELECT COALESCE(SUM(time_voice), 0) FROM users WHERE guild_id = :guildId")
    long getTotalTimeVoice(long guildId);
    
    @SqlQuery("SELECT COUNT(*) FROM users WHERE guild_id = :guildId AND time_voice > 0")
    int getMemberCountWithVoiceInGuild(long guildId);
    
    
    @SqlUpdate("DELETE FROM users WHERE id = :id AND guild_id = :guildId")
    void delete(long id, long guildId);
}
