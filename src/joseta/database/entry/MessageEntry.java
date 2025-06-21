package joseta.database.entry;

import java.sql.*;
import java.time.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;;

@DatabaseTable(tableName = "messages")
public class MessageEntry {
    @DatabaseField(id = true, generatedId = false)
    private long messageId;
    @DatabaseField
    private long guildId;
    @DatabaseField
    private long channelId;
    @DatabaseField
    private long authorId;
    @DatabaseField
    private String content;
    @DatabaseField
    private Timestamp timestamp;

    // A no-arg constructor is required by ORMLite
    private MessageEntry() {}

    public MessageEntry(long messageId, long guildId, long channelId, long authorId, String content, Instant timestamp) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = Timestamp.from(timestamp);
    }

    public long getMessageId() { return messageId; }
    public MessageEntry setMessageId(long messageId) { this.messageId = messageId; return this; }

    public long getGuildId() { return guildId; }
    public MessageEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public long getChannelId() { return channelId; }
    public MessageEntry setChannelId(long channelId) { this.channelId = channelId; return this; }

    public long getAuthorId() { return authorId; }
    public MessageEntry setAuthorId(long authorId) { this.authorId = authorId; return this; }

    public String getContent() { return content; }
    public MessageEntry setContent(String content) { this.content = content; return this; }

    public Instant getTimestamp() { return timestamp.toInstant(); }
    public MessageEntry setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); return this; }
}

