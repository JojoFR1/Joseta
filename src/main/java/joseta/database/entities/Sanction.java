package joseta.database.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity @Table(name = "sanctions")
public class Sanction {
    @Embeddable
    public record SanctionId(long guildId, int sanctionNumber) {}
    
    @EmbeddedId public SanctionId id;
    
    @Column @Convert(converter = SanctionTypeConverter.class) public SanctionType sanctionType;
    @Column public long userId;
    @Column public long moderatorId;
    @Column(columnDefinition = "TEXT") public String reason = "Aucun motif fourni.";
    @Column public Instant timestamp;
    @Column public Instant expiryTime;
    @Column public boolean isExpired = false;
    
    // A non-private and no-arg constructor is required by JPA
    protected Sanction() {}
    
    public Sanction(long guildId, int sanctionNumber, SanctionType sanctionType, long userId, long moderatorId, String reason, long expiryTime) {
        this.id = new SanctionId(guildId, sanctionNumber);
        
        this.sanctionType = sanctionType;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.timestamp = Instant.now();
        this.expiryTime = timestamp.plusSeconds(expiryTime);
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
    
    public static class SanctionTypeConverter implements AttributeConverter<SanctionType, Character> {
        @Override
        public Character convertToDatabaseColumn(SanctionType attribute) {
            return attribute.code;
        }
        
        @Override
        public SanctionType convertToEntityAttribute(Character dbData) {
            return switch (dbData) {
                case 'W' -> SanctionType.WARN;
                case 'T' -> SanctionType.TIMEOUT;
                case 'K' -> SanctionType.KICK;
                case 'B' -> SanctionType.BAN;
                default -> throw new IllegalArgumentException("Unknown SanctionType code: " + dbData);
            };
        }
    }
    
    public String getSanctionId() {
        return sanctionType.code + String.valueOf(id.sanctionNumber);
    }
    
    public Sanction setId(long guildId, int sanctionNumber) {
        this.id = new SanctionId(guildId, sanctionNumber);
        return this;
    }
    
    public Sanction setSanctionType(SanctionType sanctionType) {
        this.sanctionType = sanctionType;
        return this;
    }
    
    public Sanction setUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public Sanction setModeratorId(long moderatorId) {
        this.moderatorId = moderatorId;
        return this;
    }
    
    public Sanction setReason(String reason) {
        this.reason = reason;
        return this;
    }
    
    public Sanction setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    public Sanction setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }
    
    public Sanction setExpired(boolean expired) {
        this.isExpired = expired;
        return this;
    }
}
