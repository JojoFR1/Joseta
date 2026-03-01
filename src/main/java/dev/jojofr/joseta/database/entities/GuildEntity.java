package dev.jojofr.joseta.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Guild;

@Entity @Table(name = "guilds")
public class GuildEntity {
    @Id public long id;

    @Column public String name;
    @Column public String iconUrl;
    @Column public long ownerId;
    
    @Column public int lastSanctionId = 0;

    // A non-private and no-arg constructor is required by JPA
    protected GuildEntity() {}
    
    public GuildEntity(Guild guild) {
        this(guild.getIdLong(),
             guild.getName(),
             guild.getIconUrl(),
             guild.getOwnerIdLong());
    }
    
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
