package dev.jojofr.joseta.database.entities;

import java.time.Instant;

public class SanctionEntity {
    public long guildId;
    public int sanctionNumber;
    
    public SanctionType type;
    public long userId;
    public long moderatorId;
    public String reason = "Aucun motif fourni.";
    public Instant createdAt;
    public Instant expiresAt;
    public boolean isExpired = false;
    public boolean isPermanent = false;
    
    // A non-private and no-arg constructor is required by JDBI
    protected SanctionEntity() {}
    public SanctionEntity(long guildId, int sanctionNumber, SanctionType type, long userId, long moderatorId, String reason, long expiresAt) {
        this.guildId = guildId;
        this.sanctionNumber = sanctionNumber;
        
        this.type = type;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plusSeconds(expiresAt);
        this.isPermanent = expiresAt <= 0;
    }
    
    public enum SanctionType {
        WARN('W'),
        TIMEOUT('T'),
        KICK('K'),
        BAN('B');
        
        public final char code;
        SanctionType(char code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return switch (this) {
                case WARN -> "Avertissement";
                case TIMEOUT -> "Exclusion";
                case KICK -> "Expulsion";
                case BAN -> "Bannissement";
            };
        }
    }
    
    public String getSanctionId() {
        return type.code + String.valueOf(sanctionNumber);
    }
    
    public SanctionEntity setId(long guildId, int sanctionNumber) {
        this.guildId = guildId;
        this.sanctionNumber = sanctionNumber;
        return this;
    }
    
    public SanctionEntity setType(SanctionType type) {
        this.type = type;
        return this;
    }
    
    public SanctionEntity setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public SanctionEntity setModeratorId(long moderatorId) {
        this.moderatorId = moderatorId;
        return this;
    }
    
    public SanctionEntity setReason(String reason) {
        this.reason = reason;
        return this;
    }
    
    public SanctionEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public SanctionEntity setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }
    
    public SanctionEntity setExpired(boolean expired) {
        this.isExpired = expired;
        return this;
    }
}
