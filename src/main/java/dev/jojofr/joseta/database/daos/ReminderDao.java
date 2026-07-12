package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.ReminderEntity;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ReminderDao {
    @SqlUpdate("""
        INSERT INTO reminders (guild_id, channel_id, user_id, text, remind_at, repeat_after, dm, repeat)
        VALUES (:guildId, :channelId, :userId, :text, :remindAt, :repeatAfter, :dm, :repeat)
    """)
    void insert(@BindFields ReminderEntity reminder);
    
    @SqlUpdate("""
        UPDATE reminders SET
            guild_id = :guildId, channel_id = :channelId, user_id = :userId, text = :text,
            remind_at = :remindAt, repeat_after = :repeatAfter, dm = :dm, repeat = :repeat
        WHERE id = :id
    """)
    void update(@BindFields ReminderEntity reminder);
    
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
