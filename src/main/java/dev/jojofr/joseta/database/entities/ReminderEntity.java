package dev.jojofr.joseta.database.entities;

import jakarta.persistence.*;

import java.time.Instant;

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
    @Column public long remindAfter;
    @Column public Instant remindAt;
    @Column public boolean dm = false;
    @Column public boolean repeat = false;
    
    // A non-private and no-arg constructor is required by JPA
    protected ReminderEntity() {}
    
    public ReminderEntity(long guildId, long channelId, long userId, String message, long remindAfter, boolean dm, boolean repeat) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        
        this.message = message;
        this.createdAt = Instant.now();
        this.remindAfter = remindAfter;

        if (createdAt.getEpochSecond() + remindAfter > Instant.MAX.getEpochSecond()) this.remindAt = Instant.MAX;
        else this.remindAt = createdAt.plusSeconds(remindAfter);
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
    
    public ReminderEntity setRemindAfter(long remindAfter) {
        this.remindAfter = remindAfter;
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
