package joseta.database.entry;

import java.time.*;

public class MessageEntry {
    public final long id;
    public final long guildId;
    public final long channelId;
    public final long authorId;
    public final String content;
    public final Instant timestamp;

    public MessageEntry(long id, long guildId, long channelId, long authorId, String content, Instant timestamp) {
        this.id = id;
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = timestamp;
    }
}

