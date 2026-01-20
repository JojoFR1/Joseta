package joseta.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "guilds")
public class Guild {
    @Id public long id;

    @Column public String name;
    @Column public String iconUrl;
    @Column public long ownerId;
    
    @Column public int lastSanctionId = 0;

    // A non-private and no-arg constructor is required by JPA
    protected Guild() {}
    
    public Guild(net.dv8tion.jda.api.entities.Guild guild) {
        this(guild.getIdLong(),
             guild.getName(),
             guild.getIconUrl(),
             guild.getOwnerIdLong());
    }
    
    public Guild(long id, String name, String iconUrl, long ownerId) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.ownerId = ownerId;
    }
    
    public Guild setId(long id) {
        this.id = id;
        return this;
    }
    
    public Guild setName(String name) {
        this.name = name;
        return this;
    }
    
    public Guild setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public Guild setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }
    
    public Guild setLastSanctionId(int lastSanctionId) {
        this.lastSanctionId = lastSanctionId;
        return this;
    }
    
    public Guild incrementLastSanctionId() {
        this.lastSanctionId += 1;
        return this;
    }
}
