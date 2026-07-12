package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.SanctionEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface SanctionDao {
    @SqlUpdate("""
        INSERT INTO sanctions (guild_id, sanction_number, type, user_id, moderator_id, reason, created_at, expires_at, is_expired, is_permanent)
        VALUES (:guildId, :sanctionNumber, :type, :userId, :moderatorId, :reason, :createdAt, :expiresAt, :isExpired, :isPermanent)
        ON CONFLICT (guild_id, sanction_number) DO UPDATE SET
            type = EXCLUDED.type,
            user_id = EXCLUDED.user_id,
            moderator_id = EXCLUDED.moderator_id,
            reason = EXCLUDED.reason,
            created_at = EXCLUDED.created_at,
            expires_at = EXCLUDED.expires_at,
            is_expired = EXCLUDED.is_expired,
            is_permanent = EXCLUDED.is_permanent
    """)
    void upsert(@BindFields SanctionEntity sanction);
    
    @SqlUpdate("UPDATE sanctions SET is_expired = TRUE WHERE guild_id = :guildId AND user_id = :userId AND type = :sanctionType AND is_expired = FALSE")
    void setLatestUserSanctionByTypeAsExpired(long guildId, long userId, SanctionEntity.SanctionType sanctionType);
    
    @SqlQuery("SELECT * FROM sanctions WHERE guild_id = :guildId AND user_id = :userId ORDER BY sanction_number DESC LIMIT :limit OFFSET :offset")
    @RegisterFieldMapper(value = SanctionEntity.class)
    List<SanctionEntity> getByUserId(long guildId, long userId, int offset, int limit);
    
    @SqlQuery("SELECT * FROM sanctions WHERE is_permanent = false AND is_expired = FALSE AND expires_at <= NOW()")
    @RegisterFieldMapper(value = SanctionEntity.class)
    List<SanctionEntity> getExpiredSanctions();
}
