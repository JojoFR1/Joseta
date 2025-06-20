package joseta.database.entry;

import java.sql.*;
import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "sanctions")
public class SanctionEntry {
    @DatabaseField(id = true, generatedId = false)
    private long id;
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

    // A no-arg constructor is required by ORMLite
    private SanctionEntry() {};

    public SanctionEntry(long id, long userId, long moderatorId, long guildId, String reason, Instant timestamp, long expiryTime) {
        this.id = id;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.guildId = guildId;
        this.reason = reason;
        this.timestamp = Timestamp.from(timestamp);
        this.expiryTime = expiryTime;
    }

    public int getSanctionTypeId() {
        return Integer.parseInt(Long.toString(id).substring(0, 2));
    }

    public boolean isExpired() {
        return  expiryTime >= 1 && timestamp.toInstant().plusSeconds(expiryTime).isBefore(Instant.now());
    }

    public long getId() { return id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public long getModeratorId() { return moderatorId; }
    public void setModeratorId(long moderatorId) { this.moderatorId = moderatorId; }
    
    public long getGuildId() { return guildId; }
    public void setGuildId(long guildId) { this.guildId = guildId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Instant getTimestamp() { return timestamp.toInstant(); }
    public void setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); }
    
    public long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }

    
}
