package joseta.database.entry;

import jakarta.persistence.*;

import java.sql.*;
import java.time.*;

@Entity @Table(name = "reminders")
public class ReminderEntry {
    @Id
    private long id;
    @Column
    private long guildId;
    @Column
    private long channelId;
    @Column
    private long userId;
    @Column
    private String message;
    @Column
    private Instant time;

    // A no-arg constructor is required by JPA
    protected ReminderEntry() {}

    public ReminderEntry(long guildId, long channelId, long userId, String message, long time) {
        this.id = Instant.now().toEpochMilli();
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        this.message = message;
        this.time = Instant.now().plusSeconds(time);
    }

    public long getId() { return id; }
    public ReminderEntry setId(long id) { this.id = id; return this; }

    public long getGuildId() { return guildId; }
    public ReminderEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public long getChannelId() { return channelId; }
    public ReminderEntry setChannelId(long channelId) { this.channelId = channelId; return this; }

    public long getUserId() { return userId; }
    public ReminderEntry setUserId(long userId) { this.userId = userId; return this; }

    public String getMessage() { return message; }
    public ReminderEntry setMessage(String message) { this.message = message; return this; }

    public Instant getTime() { return time; }
    public ReminderEntry setTime(Instant time) { this.time = time; return this; }
}
