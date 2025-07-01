package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.time.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity @Table(name = "users")
public class UserEntry {
    @Embeddable
    protected record UserId(long userId, long guildId) {}

    
    @EmbeddedId @NotNull
    private UserId userId;
    @Column
    private String username;
    @Column
    private String avatarUrl;
    @Column
    private Timestamp createdAt;
    @Column
    private int sanctionCount;
    
    // A no-arg constructor is required by JPA
    protected UserEntry() {}

    public UserEntry(long userId, long guildId, String username, String avatarUrl, Instant createdAt) {
        this.userId = new UserId(userId, guildId); // Unique ID combining userId and guildId
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.createdAt = Timestamp.from(createdAt);
        this.sanctionCount = 0; // Default value
    }
    
    public UserEntry(Member user) {
        this(user.getIdLong(),
            user.getGuild().getIdLong(),
            user.getEffectiveName(),
            user.getEffectiveAvatarUrl(),
            user.getTimeCreated().toInstant()
        );
    }

    public UserId getUserId() { return userId; }
    public UserEntry setUserId(long userId, long guildId) { this.userId = new UserId(userId, guildId); return this; }

    public String getUsername() { return username; }
    public UserEntry setUsername(String username) { this.username = username; return this; }

    public String getAvatarUrl() { return avatarUrl; }
    public UserEntry setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }

    public Instant getCreatedAt() { return createdAt.toInstant(); }
    public UserEntry setCreatedAt(Instant createdAt) { this.createdAt = Timestamp.from(createdAt); return this; }

    public int getSanctionCount() { return sanctionCount; }
    public UserEntry setSanctionCount(int sanctionCount) { this.sanctionCount = sanctionCount; return this; }

    public UserEntry incrementSanctionCount() { this.sanctionCount += 1; return this; }
}
