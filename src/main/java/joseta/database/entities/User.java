package joseta.database.entities;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

@Entity @Table(name = "users")
public class User {
    @Embeddable
    public record UserId(long id, long guildId) {}
    
    @EmbeddedId public UserId id;
    
    @Column public String username;
    @Column public String avatarUrl;
    @Column public Instant creationTime;
    @Column public int sanctionCount = 0;
    
    // A non-private and no-arg constructor is required by JPA
    protected User() {}
    
    public User(Member member) {
        this(member.getIdLong(),
             member.getGuild().getIdLong(),
             member.getUser().getName(),
             member.getUser().getAvatarUrl(),
             member.getTimeCreated().toInstant());
    }
    
    public User(long id, long guildId, String username, String avatarUrl, Instant creationTime) {
        this.id = new UserId(id, guildId);
        
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.creationTime = creationTime;
    }
    
    
    public User setId(long id, long guildId) {
        this.id = new UserId(id, guildId);
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
    
    public User setSanctionCount(int sanctionCount) {
        this.sanctionCount = sanctionCount;
        return this;
    }
    
    public User incrementSanctionCount() {
        this.sanctionCount += 1;
        return this;
    }
}
