package joseta.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity @Table(name = "reminders")
public class Reminder {
    @Id public long id;
    
    @Column public long guildId;
    @Column public long channelId;
    @Column public long userId;
    @Column public String message;
    @Column public Instant time;
    
    // A non-private and no-arg constructor is required by JPA
    protected Reminder() {}
    
    public Reminder(long guildId, long channelId, long userId, String message, long time) {
        this.id = System.currentTimeMillis();
        
        this.guildId = guildId;
        this.channelId = channelId;
        this.userId = userId;
        this.message = message;
        this.time = Instant.now().plusSeconds(time);
    }
    
    
    public Reminder setId(long id) {
        this.id = id;
        return this;
    }
    
    public Reminder setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }
    
    public Reminder setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }
    
    public Reminder setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public Reminder setMessage(String message) {
        this.message = message;
        return this;
    }
    
    public Reminder setTime(Instant time) {
        this.time = time;
        return this;
    }
}
