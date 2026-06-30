package dev.jojofr.joseta.database.entities;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ReminderEntity {
    public long id;
    
     public long guildId;
     public long channelId;
     public long userId;
    
     public String text;
     public Instant createdAt;
     public Instant remindAt;
     public long repeatAfter;
     public boolean dm = false;
     public boolean repeat = false;
    
    // A non-private and no-arg constructor is required by JDBI
    protected ReminderEntity() {}
    public ReminderEntity(long guildId, long channelId, long userId, String message, Instant remindAt, long repeatAfter, boolean dm, boolean repeat) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        
        this.text = message;
        this.createdAt = Instant.now();
        this.remindAt = remindAt;
        this.repeatAfter = repeatAfter;

        this.dm = dm;
        this.repeat = repeat;
    }
    
    public ReminderEntity setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }
    
    public ReminderEntity setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }
    
    public ReminderEntity setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public ReminderEntity setText(String text) {
        this.text = text;
        return this;
    }
    
    public ReminderEntity setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt.atZone(ZoneId.of("Europe/Paris")).toInstant();
        return this;
    }
    public ReminderEntity setRemindAt(Instant remindAt) {
        this.remindAt = remindAt;
        return this;
    }
    
    public ReminderEntity setRepeatAfter(long repeatAfter) {
        this.repeatAfter = repeatAfter;
        return this;
    }
    
    public ReminderEntity setDm(boolean dm) {
        this.dm = dm;
        return this;
    }
    
    public ReminderEntity setRepeat(boolean repeat) {
        this.repeat = repeat;
        return this;
    }
}
