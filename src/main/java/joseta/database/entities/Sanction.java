package joseta.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "sanctions")
public class Sanction {
    @Id public long guildId;
    @Id public String sanctionId;
    
    @Column public long userId;
    @Column public long moderatorId;
    @Column public String reason;
    @Column public long timestamp;
    @Column public long expiryTime;
    @Column public boolean isExpired = false;
    
    // A non-private and no-arg constructor is required by JPA
    protected Sanction() {}
    
    public Sanction(long guildId, SanctionType sanctionType, int sanctionNumber, long userId, long moderatorId, String reason, long expiryTime) {
        this.guildId = guildId;
        this.sanctionId = sanctionType.code + String.valueOf(sanctionNumber);
        
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.expiryTime = timestamp + expiryTime * 1000;
    }
    
    public enum SanctionType {
        WARN('W'),
        TIMEOUT('T'),
        KICK('K'),
        BAN('B');
        
        public final char code;
        SanctionType(char code) {
            this.code = code;
        }
    }
    
    
    
    public Sanction setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }
    
    public Sanction setSanctionId(String sanctionId) {
        this.sanctionId = sanctionId;
        return this;
    }
    
    public Sanction setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public Sanction setModeratorId(long moderatorId) {
        this.moderatorId = moderatorId;
        return this;
    }
    
    public Sanction setReason(String reason) {
        this.reason = reason;
        return this;
    }
    
    public Sanction setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    public Sanction setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }
    
    public Sanction setExpired(boolean expired) {
        isExpired = expired;
        return this;
    }
}
