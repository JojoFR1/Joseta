package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.ReminderEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ReminderDao {
    @SqlUpdate("""
        INSERT INTO reminders (guild_id, channel_id, user_id, text, remind_at, repeat_after, dm, repeat)
        VALUES (:guildId, :channelId, :userId, :text, :remindAt, :repeatAfter, :dm, :repeat)
        ON CONFLICT (id) DO UPDATE SET
            guild_id = EXCLUDED.guild_id,
            channel_id = EXCLUDED.channel_id,
            user_id = EXCLUDED.user_id,
            text = EXCLUDED.text,
            remind_at = EXCLUDED.remind_at,
            repeat_after = EXCLUDED.repeat_after,
            dm = EXCLUDED.dm,
            repeat = EXCLUDED.repeat
    """)
    void upsert(@BindFields ReminderEntity reminder);
    
    @SqlQuery("SELECT * FROM reminders WHERE guild_id = :guildId AND user_id = :userId ORDER BY remind_at")
    @RegisterFieldMapper(value = ReminderEntity.class)
    List<ReminderEntity> getByUserId(long guildId, long userId);
    
    @SqlQuery("SELECT * FROM reminders WHERE remind_at <= NOW()")
    @RegisterFieldMapper(value = ReminderEntity.class)
    List<ReminderEntity> getExpiredReminders();
    
    @SqlUpdate("DELETE FROM reminders WHERE id = :id")
    void delete(long id);
    @SqlUpdate("DELETE FROM reminders WHERE user_id = :userId AND guild_id = :guildId")
    void deleteByUserId(long userId, long guildId);
    @SqlUpdate("DELETE FROM reminders WHERE guild_id = :guildId")
    void deleteByGuildId(long guildId);
}
