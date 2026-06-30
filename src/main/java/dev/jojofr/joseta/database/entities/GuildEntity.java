package dev.jojofr.joseta.database.entities;

import net.dv8tion.jda.api.entities.Guild;

public class GuildEntity {
    public long id;

    public String name;
    public String iconUrl;
    public long ownerId;
    
    public int lastSanctionId = 0;

    // A non-private and no-arg constructor is required by JDBI
    protected GuildEntity() {}
    public GuildEntity(Guild guild) { this(guild.getIdLong(), guild.getName(), guild.getIconUrl(), guild.getOwnerIdLong()); }
    public GuildEntity(long id, String name, String iconUrl, long ownerId) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.ownerId = ownerId;
    }
    
    public GuildEntity setId(long id) {
        this.id = id;
        return this;
    }
    
    public GuildEntity setName(String name) {
        this.name = name;
        return this;
    }
    
    public GuildEntity setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public GuildEntity setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }
    
    public GuildEntity setLastSanctionId(int lastSanctionId) {
        this.lastSanctionId = lastSanctionId;
        return this;
    }
    
    public GuildEntity incrementLastSanctionId() {
        this.lastSanctionId += 1;
        return this;
    }
}
