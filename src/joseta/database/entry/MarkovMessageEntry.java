package joseta.database.entry;

import java.sql.*;
import java.time.*;

import jakarta.persistence.*;

@Entity @Table(name = "markov_messages")
public class MarkovMessageEntry {
    @Id
    private long messageId;
    @Column
    private long guildId;
    @Column
    private long channelId;
    @Column
    private long authorId;
    @Column
    private String content;
    @Column
    private Timestamp timestamp;

    // No-arg constructor required by ORMLite
    private MarkovMessageEntry() {}

    public MarkovMessageEntry(long messageId, long guildId, long channelId, long authorId, String content, Instant timestamp) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = Timestamp.from(timestamp);
    }

    public long getMessageId() { return messageId; }
    public MarkovMessageEntry setMessageId(long messageId) { this.messageId = messageId; return this; }
    
    public long getGuildId() { return guildId; }
    public MarkovMessageEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

    public long getChannelId() { return channelId; }
    public MarkovMessageEntry setChannelId(long channelId) { this.channelId = channelId; return this; }

    public long getAuthorId() { return authorId; }
    public MarkovMessageEntry setAuthorId(long authorId) { this.authorId = authorId; return this; }

    public String getContent() { return content; }
    public MarkovMessageEntry setContent(String content) { this.content = content; return this; }

    public Instant getTimestamp() { return timestamp.toInstant(); }
    public MarkovMessageEntry setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); return this; }
}
