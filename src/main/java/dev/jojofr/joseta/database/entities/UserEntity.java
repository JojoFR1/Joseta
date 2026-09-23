package dev.jojofr.joseta.database.entities;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

public class UserEntity {
    public long id;
    public long guildId;
    
    public String name;
    public String avatarUrl;
    public Instant creationDate;
    
    public int sanctionCount = 0;
    public long timeVoice = 0;
    public int countingSuccess = 0;
    public int countingFail = 0;
    public int countingSpecialSuccess = 0;
    public int countingSpecialFail = 0;
    
    // A non-private and no-arg constructor is required by JDBI
    protected UserEntity() {}
    public UserEntity(Member member) { this(member.getUser(), member.getGuild().getIdLong()); }
    public UserEntity(User user, long guildId) { this(user.getIdLong(), guildId, user.getName(), user.getAvatarUrl(), user.getTimeCreated().toInstant());}
    public UserEntity(long id, long guildId, String name, String avatarUrl, Instant creationDate) {
        this.id = id;
        this.guildId = guildId;
        
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.creationDate = creationDate;
    }
    
    
    public UserEntity setId(long id, long guildId) {
        this.id = id;
        this.guildId = guildId;
        return this;
    }

    public UserEntity setName(String name) {
        this.name = name;
        return this;
    }

    public UserEntity setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public UserEntity setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
        return this;
    }
    
    public UserEntity setSanctionCount(int sanctionCount) {
        this.sanctionCount = sanctionCount;
        return this;
    }
    
    public UserEntity incrementSanctionCount() {
        this.sanctionCount += 1;
        return this;
    }
    
    public UserEntity setTimeVoice(long timeVoice) {
        this.timeVoice = timeVoice;
        return this;
    }
    
    public UserEntity setCountingSuccess(int countingSuccess) {
        this.countingSuccess = countingSuccess;
        return this;
    }
    
    public UserEntity setCountingFail(int countingFail) {
        this.countingFail = countingFail;
        return this;
    }
    
    public UserEntity setCountingSpecialSuccess(int countingSpecialSuccess) {
        this.countingSpecialSuccess = countingSpecialSuccess;
        return this;
    }
    
    public UserEntity setCountingSpecialFail(int countingSpecialFail) {
        this.countingSpecialFail = countingSpecialFail;
        return this;
    }
}
