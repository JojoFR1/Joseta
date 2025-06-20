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

    public long getGuildId() { return guildId; }
    public void setGuildId(long guildId) { this.guildId = guildId; }

    public long getChannelId() { return channelId; }
    public void setChannelId(long channelId) { this.channelId = channelId; }

    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp.toInstant(); }
    public void setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); }

    @Override
    public String toString() {
        return "MessageEntry{" +
                "messageId=" + messageId +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                ", authorId=" + authorId +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

