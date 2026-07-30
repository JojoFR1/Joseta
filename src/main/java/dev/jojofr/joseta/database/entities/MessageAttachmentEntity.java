package dev.jojofr.joseta.database.entities;

import net.dv8tion.jda.api.entities.Message;

public class MessageAttachmentEntity {
    public long id;
    
    public long messageId;
    public String filename;
    public String extension;
    public String description;
    public String contentType;
    public String url;
    public String proxyUrl;
    public int size;
    public int width;
    public int height;
    
    // A non-private and no-arg constructor is required by JDBI
    protected MessageAttachmentEntity() {}
    public MessageAttachmentEntity(long messageId, Message.Attachment attachment) {
        this(attachment.getIdLong(),
            messageId,
            attachment.getFileName(),
            attachment.getFileExtension(),
            attachment.getDescription(),
            attachment.getContentType(),
            attachment.getUrl(),
            attachment.getProxyUrl(),
            attachment.getSize(),
            attachment.getWidth(),
            attachment.getHeight()
        );
    }
    public MessageAttachmentEntity(long id, long messageId, String filename, String extension, String description, String contentType, String url, String proxyUrl, int size, int width, int height) {
        this.id = id;
        
        this.messageId = messageId;
        this.filename = filename;
        this.extension = extension;
        this.description = description;
        this.contentType = contentType;
        this.url = url;
        this.proxyUrl = proxyUrl;
        this.size = size;
        this.width = width;
        this.height = height;
    }
    
    public MessageAttachmentEntity setId(long id) {
        this.id = id;
        return this;
    }
    
    public MessageAttachmentEntity setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }
    
    public MessageAttachmentEntity setFilename(String filename) {
        this.filename = filename;
        return this;
    }
    
    public MessageAttachmentEntity setExtension(String extension) {
        this.extension = extension;
        return this;
    }
    
    public MessageAttachmentEntity setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public MessageAttachmentEntity setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    public MessageAttachmentEntity setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public MessageAttachmentEntity setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
        return this;
    }
    
    public MessageAttachmentEntity setSize(int size) {
        this.size = size;
        return this;
    }
    
    public MessageAttachmentEntity setWidth(int width) {
        this.width = width;
        return this;
    }
    
    public MessageAttachmentEntity setHeight(int height) {
        this.height = height;
        return this;
    }
}
