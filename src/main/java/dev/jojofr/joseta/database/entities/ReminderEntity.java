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
    @Column public Instant remindAt;
    
    // A non-private and no-arg constructor is required by JPA
    protected ReminderEntity() {}
    
    public ReminderEntity(long guildId, long channelId, long userId, String message, Instant remindAt) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        this.message = message;
        this.remindAt = remindAt;
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
    
    public ReminderEntity setRemindAt(Instant remindAt) {
        this.remindAt = remindAt;
        return this;
    }
}
