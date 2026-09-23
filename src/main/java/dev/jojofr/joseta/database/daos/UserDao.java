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
    
    @SqlUpdate("UPDATE users SET time_voice = time_voice + :timeVoice WHERE id = :id AND guild_id = :guildId")
    int addTimeVoice(long id, long guildId, long timeVoice);
    
    @SqlUpdate("UPDATE users SET counting_success = counting_success + 1 WHERE id = :id AND guild_id = :guildId")
    int incrementCountingSuccess(long id, long guildId);
    @SqlUpdate("UPDATE users SET counting_fail = counting_fail + 1 WHERE id = :id AND guild_id = :guildId")
    int incrementCountingFail(long id, long guildId);
    @SqlUpdate("UPDATE users SET counting_special_success = counting_special_success + 1 WHERE id = :id AND guild_id = :guildId")
    int incrementCountingSpecialSuccess(long id, long guildId);
    @SqlUpdate("UPDATE users SET counting_special_fail = counting_special_fail + 1 WHERE id = :id AND guild_id = :guildId")
    int incrementCountingSpecialFail(long id, long guildId);
    
    
    @SqlQuery("SELECT id, time_voice as count FROM users WHERE guild_id = :guildId AND time_voice > 0 ORDER BY time_voice DESC LIMIT :limit OFFSET :offset")
    @RegisterConstructorMapper(value = LeaderboardEntry.class)
    List<LeaderboardEntry> getVoiceLeaderboard(long guildId, int limit, int offset);
    
    @SqlQuery("SELECT * FROM users WHERE id = :id AND guild_id = :guildId")
    @RegisterFieldMapper(value = UserEntity.class)
    UserEntity getById(long id, long guildId);
    
    @SqlQuery("SELECT SUM(time_voice) FROM users WHERE guild_id = :guildId")
    long getTotalTimeVoice(long guildId);
    
    @SqlQuery("SELECT COUNT(*) FROM users WHERE guild_id = :guildId AND time_voice > 0")
    int getMemberCountWithVoiceInGuild(long guildId);
    
    
    @SqlUpdate("DELETE FROM users WHERE id = :id AND guild_id = :guildId")
    void delete(long id, long guildId);
}
