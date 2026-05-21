package dev.jojofr.joseta.database.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@SuppressWarnings("unused")
@Entity @Table(name = "reminders")
public class ReminderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column public long guildId;
    @Column public long channelId;
    @Column public long userId;
    
    @Column(columnDefinition = "TEXT") public String message;
    @Column public Instant createdAt;
    @Column public Instant remindAt;
    @Column public long repeatAfter;
    @Column public boolean dm = false;
    @Column public boolean repeat = false;
    
    // A non-private and no-arg constructor is required by JPA
    protected ReminderEntity() {}
    
    public ReminderEntity(long guildId, long channelId, long userId, String message, Instant remindAt, long repeatAfter, boolean dm, boolean repeat) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        
        this.message = message;
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
    
    public ReminderEntity setMessage(String message) {
        this.message = message;
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
