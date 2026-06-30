package dev.jojofr.joseta.database.entities;

import java.time.OffsetDateTime;

public class MessageEntity {
    public long id;
    
    public long guildId;
    public long channelId;
    public long authorId;
    public String content;
    public String markovContent;
    public OffsetDateTime createdAt;
    
    // A non-private and no-arg constructor is required by JDBI
    protected MessageEntity() {}
    public MessageEntity(long id, long guildId, long channelId, long authorId, String content, String markovContent, OffsetDateTime createdAt) {
        this.id = id;
        
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.markovContent = markovContent;
        this.createdAt = createdAt;
    }
    
    public MessageEntity setId(long id) {
        this.id = id;
        return this;
    }
    
    public MessageEntity setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }
    
    public MessageEntity setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }
    
    public MessageEntity setAuthorId(long authorId) {
        this.authorId = authorId;
        return this;
    }
    
    public MessageEntity setContent(String content) {
        this.content = content;
        return this;
    }
    
    public MessageEntity setMarkovContent(String markovContent) {
        this.markovContent = markovContent;
        return this;
    }
    
    public MessageEntity setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
