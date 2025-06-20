package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "users")
public class UserEntry {
    @DatabaseField(id = true, generatedId = false)
    private long userId;
    @DatabaseField()
    private String username;
    @DatabaseField
    private String discriminator;
    @DatabaseField
    private String avatarUrl;
    @DatabaseField
    private OffsetDateTime createdAt;
    @DatabaseField
    private int sanctionCount;
    
    // A no-arg constructor is required by ORMLite
    private UserEntry() {}

    public UserEntry(long userId, String username, String discriminator, String avatarUrl, OffsetDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.discriminator = discriminator;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.sanctionCount = 0; // Default value
    }
    
    public UserEntry(User user) {
        this(user.getIdLong(),
            user.getName(),
            user.getDiscriminator(),
            user.getEffectiveAvatarUrl(),
            user.getTimeCreated()
        );
    }
}
