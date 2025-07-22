package joseta.database.entry;

import java.sql.*;
import java.time.*;

import jakarta.persistence.*;;

@Entity @Table(name = "messages")
public class MessageEntry {
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
    private String markovContent;
    @Column
    private Timestamp timestamp;

    // A no-arg constructor is required by JPA
    protected MessageEntry() {}

    public MessageEntry(long messageId, long guildId, long channelId, long authorId, String content, String markovContent, Instant timestamp) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.markovContent = markovContent;
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

    public String getMarkovContent() { return markovContent; }
    public MessageEntry setMarkovContent(String markovContent) { this.markovContent = markovContent; return this; }

    public Instant getTimestamp() { return timestamp.toInstant(); }
    public MessageEntry setTimestamp(Instant timestamp) { this.timestamp = Timestamp.from(timestamp); return this; }
}

