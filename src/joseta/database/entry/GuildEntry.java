package joseta.database.entry;

import net.dv8tion.jda.api.entities.*;

import org.hibernate.annotations.*;

import jakarta.persistence.*;

@Entity @Table(name = "guilds")
public class GuildEntry {
    @Id
    private long guildId;
    @Column
    private String name;
    @Column
    private String iconUrl;
    @Column
    private long ownerId;
    @Column @ColumnDefault("-1")
    private int lastSanctionId;
    
    // A no-arg constructor is required by JPA
    protected GuildEntry() {}

    public GuildEntry(GuildEntry other) {
        this.guildId = other.guildId;
        this.name = other.name;
        this.iconUrl = other.iconUrl;
        this.ownerId = other.ownerId;
        this.lastSanctionId = other.lastSanctionId;
    }

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

    public long getGuildId() { return guildId; }
    public GuildEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public String getName() { return name; }
    public GuildEntry setName(String name) { this.name = name; return this; }

    public String getIconUrl() { return iconUrl; }
    public GuildEntry setIconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }

    public long getOwnerId() { return ownerId; }
    public GuildEntry setOwnerId(long ownerId) { this.ownerId = ownerId; return this; }

    public int getLastSanctionId() { return lastSanctionId; }
    public GuildEntry setLastSanctionId(int lastSanctionId) { this.lastSanctionId = lastSanctionId; return this; }
    public GuildEntry incrementLastSanctionId() { this.lastSanctionId++; return this; }
}
