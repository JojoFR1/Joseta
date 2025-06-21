package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "users")
public class UserEntry {
    @DatabaseField(id = true, generatedId = false)
    private long userId;
    @DatabaseField(id = true, generatedId = false)
    private long guildId; // This field is added to allow multiple entries for the same user in different guilds
    @DatabaseField
    private String username;
    @DatabaseField
    private String avatarUrl;
    @DatabaseField
    private OffsetDateTime createdAt;
    @DatabaseField
    private int sanctionCount;
    
    // A no-arg constructor is required by ORMLite
    private UserEntry() {}

    public UserEntry(long userId, long guildId, String username, String avatarUrl, OffsetDateTime createdAt) {
        this.userId = userId;
        this.guildId = guildId; // This allows the same user to have different entries in different guilds
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.sanctionCount = 0; // Default value
    }
    
    public UserEntry(Member user) {
        this(user.getIdLong(),
            user.getGuild().getIdLong(),
            user.getEffectiveName(),
            user.getEffectiveAvatarUrl(),
            user.getTimeCreated()
        );
    }

    public long getUserId() { return userId; }
    public UserEntry setUserId(long userId) { this.userId = userId; return this; }

    public long getGuildId() { return guildId; }
    public UserEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public String getUsername() { return username; }
    public UserEntry setUsername(String username) { this.username = username; return this; }

    public String getAvatarUrl() { return avatarUrl; }
    public UserEntry setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public UserEntry setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

    public int getSanctionCount() { return sanctionCount; }
    public UserEntry setSanctionCount(int sanctionCount) { this.sanctionCount = sanctionCount; return this; }

    public UserEntry incrementSanctionCount() { this.sanctionCount += 1; return this; }
}
