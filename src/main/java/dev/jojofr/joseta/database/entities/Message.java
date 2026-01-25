package dev.jojofr.joseta.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity @Table(name = "messages")
public class Message {
    @Id public long id;
    
    @Column public long guildId;
    @Column public long channelId;
    @Column public long authorId;
    @Column(columnDefinition = "TEXT") public String content;
    @Column(columnDefinition = "TEXT") public String markovContent;
    @Column public OffsetDateTime creationTime;
    
    // A non-private and no-arg constructor is required by JPA
    protected Message() {}
    
    public Message(long id, long guildId, long channelId, long authorId, String content, String markovContent, OffsetDateTime timestamp) {
        this.id = id;
        
        this.guildId = guildId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.content = content;
        this.markovContent = markovContent;
        this.creationTime = timestamp;
    }
    
    
    public Message setId(long id) {
        this.id = id;
        return this;
    }
    
    public Message setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }
    
    public Message setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }
    
    public Message setAuthorId(long authorId) {
        this.authorId = authorId;
        return this;
    }
    
    public Message setContent(String content) {
        this.content = content;
        return this;
    }
    
    public Message setMarkovContent(String markovContent) {
        this.markovContent = markovContent;
        return this;
    }
    
    public Message setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }
}
