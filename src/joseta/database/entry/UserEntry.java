package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.time.*;

import jakarta.persistence.*;

@Entity @Table(name = "users")
public class UserEntry {
    @Id
    private String id;
    @Column
    private long userId;
    @Column
    private long guildId;
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
        this.id = userId + "-" + guildId; // Unique ID combining userId and guildId
        this.userId = userId;
        this.guildId = guildId;
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

    public String getId() { return id; }
    public UserEntry setId(long userId, long guildId) { this.id = userId + "-" + guildId; return this; }

    public long getUserId() { return userId; }
    public UserEntry setUserId(long userId) { this.userId = userId; return this; }

    public long getGuildId() { return guildId; }
    public UserEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

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
