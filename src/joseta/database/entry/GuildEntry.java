package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "guilds")
public class GuildEntry {
    @DatabaseField(id = true, generatedId = false)
    private long guildId;
    @DatabaseField
    private String name;
    @DatabaseField
    private String iconUrl;
    @DatabaseField
    private long ownerId;
    @DatabaseField(defaultValue = "-1")
    private int lastWarnId;
    @DatabaseField(defaultValue = "-1")
    private int lastTimeoutId;
    @DatabaseField(defaultValue = "-1")
    private int lastKickId;
    @DatabaseField(defaultValue = "-1")
    private int lastBanId;

    
    // A no-arg constructor is required by ORMLite
    private GuildEntry() {}

    public GuildEntry(long guildId, String name, String iconUrl, long ownerId) {
        this.guildId = guildId;
        this.name = name;
        this.iconUrl = iconUrl;
        this.ownerId = ownerId;
    }

    public GuildEntry(Guild guild) {
        this(guild.getIdLong(),
            guild.getName(),
            guild.getIconUrl() != null ? guild.getIconUrl() : "",
            guild.getOwnerIdLong()
        );
    }
}
