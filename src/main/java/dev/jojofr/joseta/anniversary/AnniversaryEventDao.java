package dev.jojofr.joseta.anniversary;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

public interface AnniversaryEventDao {
    record TemporaryRole(long guildId, long userId, long roleId, Instant expiresAt) {}
    record PullResult(int cardId, int copies, int totalPulls) {}
    
    @SqlUpdate(
        """
            INSERT INTO event_temporary_roles (guild_id, user_id, role_id, expires_at)
            VALUES (:guildId, :userId, :roleId, :expiresAt)
            ON CONFLICT (guild_id, user_id, role_id) DO UPDATE
            SET expires_at = GREATEST(event_temporary_roles.expires_at, EXCLUDED.expires_at)
        """
    )
    void upsertTemporaryRole(long guildId, long userId, long roleId, Instant expiresAt);
    
    @SqlQuery("""
        SELECT guild_id, user_id, role_id, expires_at
        FROM event_temporary_roles
        WHERE guild_id = :guildId AND user_id = :userId
    """)
    @RegisterConstructorMapper(TemporaryRole.class)
    List<TemporaryRole> getTemporaryRolesForUser(long guildId, long userId);
    
    @SqlQuery("""
        SELECT guild_id, user_id, role_id, expires_at
        FROM event_temporary_roles
        WHERE expires_at <= :now
        ORDER BY expires_at
    """)
    @RegisterConstructorMapper(TemporaryRole.class)
    List<TemporaryRole> getExpiredTemporaryRoles(Instant now);
    
    @SqlUpdate("DELETE FROM event_temporary_roles WHERE guild_id = :guildId AND user_id = :userId AND role_id = :roleId")
    void deleteTemporaryRole(long guildId, long userId, long roleId);
    
    
    
    @SqlQuery("""
        INSERT INTO event_gacha_users (user_id, last_pull_at, pulls)
        VALUES (:userId, :now, 1)
        ON CONFLICT (user_id) DO UPDATE
            SET last_pull_at = excluded.last_pull_at, pulls = event_gacha_users.pulls + 1
            WHERE event_gacha_users.last_pull_at <= :cooldownStart
        RETURNING pulls
    """)
    Integer claimPull(long userId, Instant now, Instant cooldownStart);
    
    @SqlQuery("""
        INSERT INTO event_gacha_cards (user_id, card_id)
        VALUES (:userId, :cardId)
        ON CONFLICT (user_id, card_id) DO UPDATE SET copies = event_gacha_cards.copies + 1
        RETURNING copies
    """)
    int addCard(long userId, int cardId);
    
    @SqlQuery("SELECT last_pull_at FROM event_gacha_users WHERE user_id = :userId")
    Optional<Instant> lastPull(long userId);
    
    @SqlQuery("SELECT card_id FROM event_gacha_cards WHERE user_id = :userId ORDER BY card_id")
    List<Integer> ownedCards(long userId);
    
    @Transaction
    default PullResult pull(long userId, Instant now, Duration cooldown, IntSupplier roll) {
        Integer totalPulls = claimPull(userId, now, now.minus(cooldown));
        if (totalPulls == null) return null;
        
        int cardId = roll.getAsInt();
        int copies = addCard(userId, cardId);
        return new PullResult(cardId, copies, totalPulls);
    }
    
    
    @SqlUpdate("INSERT INTO event_announcements (announcement_key, sent_at) VALUES (:key, :sentAt) ON CONFLICT DO NOTHING")
    int claimAnnouncement(String key, Instant sentAt);
    
    @SqlUpdate("DELETE FROM event_announcements WHERE announcement_key = :key")
    void releaseAnnouncement(String key);
    
    @SqlQuery("SELECT announcement_key FROM event_announcements")
    List<String> sentAnnouncements();
    
    @SqlQuery("SELECT MAX(sent_at) FROM event_announcements")
    Optional<Instant> lastAnnouncementAt();
}
