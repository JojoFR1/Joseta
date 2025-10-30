package joseta.database.entities;

import jakarta.persistence.*;

import java.time.*;

@Entity @Table(name = "users")
public class User {
    @Id long id;
    @Id long guildId;

    @Column String username;
    @Column String avatarUrl;
    @Column Instant creationTime;

    // A no-arg constructor is required by JPA
    protected User() {}

    public User(long id, long guildId, String username, String avatarUrl, Instant creationTime) {
        this.id = id;
        this.guildId = guildId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.creationTime = creationTime;
    }

    public long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public long getGuildId() {
        return guildId;
    }

    public User setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public User setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public User setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}
