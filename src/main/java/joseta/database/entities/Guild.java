package joseta.database.entities;

import jakarta.persistence.*;

@Entity @Table(name = "guilds")
public class Guild {
    @Id public long id;

    @Column public String name;
    @Column public String iconUrl;
    @Column public long ownerId;
    
    @Column public int lastSanctionId = -1;

    // A non-private and no-arg constructor is required by JPA
    protected Guild() {}

    public Guild(long id, String name) {
        this.id = id;
        this.name = name;
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
