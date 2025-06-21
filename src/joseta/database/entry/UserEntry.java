package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "users")
public class UserEntry {
    @DatabaseField(id = true, generatedId = false)
    private String id;
    @DatabaseField
    private long userId;
    @DatabaseField
    private long guildId;
    @DatabaseField
    private String username;
    @DatabaseField
    private String avatarUrl;
    @DatabaseField
    private Timestamp createdAt;
    @DatabaseField
    private int sanctionCount;
    
    // A no-arg constructor is required by ORMLite
    private UserEntry() {}

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
