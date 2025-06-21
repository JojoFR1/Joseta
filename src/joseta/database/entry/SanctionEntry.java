package joseta.database.entry;

import java.sql.*;
import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "sanctions")
public class SanctionEntry {
    @DatabaseField(id = true, generatedId = false)
    private String sanctionId;
    @DatabaseField
    private long userId;
    @DatabaseField
    private long moderatorId;
    @DatabaseField
    private long guildId;
    @DatabaseField
    private String reason;
    @DatabaseField
    private Timestamp timestamp;
    @DatabaseField
    private long expiryTime;
    @DatabaseField(defaultValue = "false")
    private boolean isExpired;

    // A no-arg constructor is required by ORMLite
    private SanctionEntry() {};

    public SanctionEntry(long sanctionId, char sanctionType, long userId, long moderatorId, long guildId, String reason, Instant timestamp, long expiryTime) {
        this.sanctionId = sanctionType + Long.toString(sanctionId);
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.guildId = guildId;
        this.reason = reason;
        this.timestamp = Timestamp.from(timestamp);
        this.expiryTime = expiryTime;
    }

    public String getFullSanctionId() { return sanctionId; }
    public long getSanctionId() { return Long.valueOf(sanctionId.substring(1)); }
    public char getSanctionTypeId() { return sanctionId.charAt(0); }
    public String getSanctionType() {
        return getSanctionTypeId() == 'W' ? "Warn"
             : getSanctionTypeId() == 'M' ? "Mute"
             : getSanctionTypeId() == 'K' ? "Kick"
             : getSanctionTypeId() == 'B' ? "Kick"
             : "Inconnu";
    }
    public SanctionEntry setSanctionId(long sanctionId, char sanctionType) { this.sanctionId = sanctionType + Long.toString(sanctionId); return this; }

    public long getUserId() { return userId; }
    public SanctionEntry setUserId(long userId) { this.userId = userId; return this; }
    
    public long getModeratorId() { return moderatorId; }
    public SanctionEntry setModeratorId(long moderatorId) { this.moderatorId = moderatorId; return this; }
    
    public long getGuildId() { return guildId; }
    public SanctionEntry setGuildId(long guildId) { this.guildId = guildId; return this; }
    
    public String getReason() { return reason; }
    public SanctionEntry setReason(String reason) { this.reason = reason; return this; }
    
    public Instant getTimestamp() { return timestamp.toInstant(); }
    public SanctionEntry setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); return this; }
    
    public long getExpiryTime() { return expiryTime; }
    public SanctionEntry setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; return this; }

    public boolean isExpired() { return isExpired; }
    public SanctionEntry setExpired(boolean isExpired) { this.isExpired = isExpired; return this; }
}
