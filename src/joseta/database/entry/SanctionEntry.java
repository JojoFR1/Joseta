package joseta.database.entry;

import java.time.*;

public class SanctionEntry {
    public final long id;
    public final long userId;
    public final long moderatorId;
    public final long guildId;
    public final String reason;
    public final Instant at;
    public final long time; 

    public SanctionEntry(long id, long userId, long moderatorId, long guildId, String reason, Instant at, long time) {
        this.id = id;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.guildId = guildId;
        this.reason = reason;
        this.at = at;
        this.time = time;
    }

    public int getSanctionTypeId() {
        return Integer.parseInt(Long.toString(id).substring(0, 2));
    }

    public boolean isExpired() {
        return at.plusSeconds(time).isBefore(Instant.now()) && time >= 1;
    }
}
