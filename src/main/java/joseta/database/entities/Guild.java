package joseta.database.entities;

import jakarta.persistence.*;

@Entity @Table(name = "guilds")
public class Guild {
    @Id long id;

    @Column String name;
    @Column String iconUrl;
    @Column long ownerId;

    // A no-arg constructor is required by JPA
    protected Guild() {}

    public Guild(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Guild setName(String name) {
        this.name = name;
        return this;
    }

    public long getId() {
        return id;
    }

    public Guild setId(long id) {
        this.id = id;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Guild setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Guild setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }
}
