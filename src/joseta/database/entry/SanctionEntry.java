package joseta.database.entry;

import java.time.*;

import org.hibernate.annotations.*;

import jakarta.persistence.*;

@Entity @Table(name = "sanctions")
public class SanctionEntry {
    // Might need an EmbeddedId in the future?
    @Id
    private String sanctionId;
    @Id
    private long guildId;
    @Column
    private long userId;
    @Column
    private long moderatorId;
    @Column
    private String reason;
    @Column
    private Instant timestamp;
    @Column
    private Instant expiryTime;
    @Column @ColumnDefault("false")
    private boolean isExpired;

    // A no-arg constructor is required by JPA
    protected SanctionEntry() {}

    public SanctionEntry(long sanctionNumber, char sanctionType, long guildId, long userId, long moderatorId, String reason, long expiryTime) {
        this.sanctionId = sanctionType + Long.toString(sanctionNumber);
        this.guildId = guildId;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.timestamp = Instant.now();
        this.expiryTime = timestamp.plusSeconds(expiryTime);
    }

    public String getSanctionIdFull() { return sanctionId; }
    public long getSanctionIdNumber() { return Long.valueOf(sanctionId.substring(1)); }
    public char getSanctionTypeId() { return sanctionId.charAt(0); }
    public String getSanctionType() {
        return getSanctionTypeId() == 'W' ? "Avertissement"
             : getSanctionTypeId() == 'T' ? "Exclusion"
             : getSanctionTypeId() == 'K' ? "Expulsion"
             : getSanctionTypeId() == 'B' ? "Bannissement"
             : "Inconnu";
    }

    public long getGuildId() { return guildId; }
    public SanctionEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public long getUserId() { return userId; }
    public SanctionEntry setUserId(long userId) { this.userId = userId; return this; }
    
    public long getModeratorId() { return moderatorId; }
    public SanctionEntry setModeratorId(long moderatorId) { this.moderatorId = moderatorId; return this; }
        
    public String getReason() { return reason; }
    public SanctionEntry setReason(String reason) { this.reason = reason; return this; }
    
    public Instant getTimestamp() { return timestamp; }
    public SanctionEntry setTimestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
    
    public Instant getExpiryTime() { return expiryTime; }
    public SanctionEntry setExpiryTime(Instant expiryTime) { this.expiryTime = expiryTime; return this; }

    public boolean isExpired() { return isExpired; }
    public SanctionEntry setExpired(boolean isExpired) { this.isExpired = isExpired; return this; }
}
