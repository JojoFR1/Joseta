package joseta.database.entities;

import jakarta.persistence.*;

import java.time.*;

@Entity @Table(name = "users")
public class User {
    @Id public long id;
    @Id public long guildId;
    
    @Column public String username;
    @Column public String avatarUrl;
    @Column public Instant creationTime;
    
    // A non-private and no-arg constructor is required by JPA
    protected User() {}
    
    public User(long id, long guildId, String username, String avatarUrl, Instant creationTime) {
        this.id = id;
        
        this.guildId = guildId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.creationTime = creationTime;
    }
    
    
    public User setId(long id) {
        this.id = id;
        return this;
    }

    public User setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public User setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public User setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}
