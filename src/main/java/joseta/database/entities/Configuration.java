package joseta.database.entities;

import jakarta.persistence.*;

import java.lang.reflect.*;

@Entity @Table(name = "configurations")
public class Configuration {
    @Id public long guildId;

    @Column public boolean welcomeEnabled = false;
    @Column public long welcomeChannelId = 0L;
    @Column public boolean welcomeImageEnabled = false;
    @Column public String welcomeJoinMessage = "Bienvenue {{user}} !";
    @Column public String welcomeLeaveMessage = "**{{userName}}** nous a quitté...";
    @Column public String welcomeImageUrl = ""; // Could switch to file upload using modal
    @Column public long joinRoleId = 0L;
    @Column public long joinBotRoleId = 0L;
    @Column public long verifiedRoleId = 0L;
    
    @Column public boolean markovEnabled = false;
    @Column public String markovBlackList = "";
    
    @Column public boolean moderationEnabled = true;
    
    @Column public String rules = "";

    @Column public boolean autoResponseEnabled = false;

    @Column public boolean countingEnabled = false;
    @Column public boolean countingCommentsEnabled = false;
    @Column public boolean countingPenaltyEnabled = false;
    @Column public long countingChannelId = 0L;
    
    // A non-private and no-arg constructor is required by JPA
    protected Configuration() {}

    public Configuration(long guildId) { this.guildId = guildId; }

    public Configuration(Configuration other) {
        this.guildId = other.guildId;

        this.welcomeEnabled = other.welcomeEnabled;
        this.welcomeChannelId = other.welcomeChannelId;
        this.welcomeImageEnabled = other.welcomeImageEnabled;
        this.welcomeJoinMessage = other.welcomeJoinMessage;
        this.welcomeLeaveMessage = other.welcomeLeaveMessage;
        this.joinRoleId = other.joinRoleId;
        this.joinBotRoleId = other.joinBotRoleId;
        this.verifiedRoleId = other.verifiedRoleId;

        this.markovEnabled = other.markovEnabled;
        this.markovBlackList = other.markovBlackList;

        this.moderationEnabled = other.moderationEnabled;

        this.autoResponseEnabled = other.autoResponseEnabled;

        this.countingEnabled = other.countingEnabled;
        this.countingCommentsEnabled = other.countingCommentsEnabled;
        this.countingPenaltyEnabled = other.countingPenaltyEnabled;
        this.countingChannelId = other.countingChannelId;
    }
    

    public Configuration setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    public Configuration setWelcomeEnabled(boolean welcomeEnabled) {
        this.welcomeEnabled = welcomeEnabled;
        return this;
    }

    public Configuration setWelcomeImageEnabled(boolean welcomeImageEnabled) {
        this.welcomeImageEnabled = welcomeImageEnabled;
        return this;
    }

    public Configuration setWelcomeChannelId(long welcomeChannelId) {
        this.welcomeChannelId = welcomeChannelId;
        return this;
    }

    public Configuration setWelcomeJoinMessage(String welcomeJoinMessage) {
        this.welcomeJoinMessage = welcomeJoinMessage;
        return this;
    }

    public Configuration setWelcomeLeaveMessage(String welcomeLeaveMessage) {
        this.welcomeLeaveMessage = welcomeLeaveMessage;
        return this;
    }
    
    public Configuration setWelcomeImageUrl(String welcomeImageUrl) {
        this.welcomeImageUrl = welcomeImageUrl;
        return this;
    }

    public Configuration setJoinRoleId(long joinRoleId) {
        this.joinRoleId = joinRoleId;
        return this;
    }

    public Configuration setJoinBotRoleId(long joinBotRoleId) {
        this.joinBotRoleId = joinBotRoleId;
        return this;
    }
    
    public Configuration setVerifiedRoleId(long verifiedRoleId) {
        this.verifiedRoleId = verifiedRoleId;
        return this;
    }

    public Configuration setMarkovEnabled(boolean markovEnabled) {
        this.markovEnabled = markovEnabled;
        return this;
    }

    public Configuration setMarkovBlackList(String markovBlackList) {
        this.markovBlackList = markovBlackList;
        return this;
    }

    public Configuration setModerationEnabled(boolean moderationEnabled) {
        this.moderationEnabled = moderationEnabled;
        return this;
    }
    
    public Configuration setRules(String rules) {
        this.rules = rules;
        return this;
    }
    
    public Configuration setAutoResponseEnabled(boolean autoResponseEnabled) {
        this.autoResponseEnabled = autoResponseEnabled;
        return this;
    }

    public Configuration setCountingEnabled(boolean countingEnabled) {
        this.countingEnabled = countingEnabled;
        return this;
    }

    public Configuration setCountingCommentsEnabled(boolean countingCommentsEnabled) {
        this.countingCommentsEnabled = countingCommentsEnabled;
        return this;
    }

    public Configuration setCountingPenaltyEnabled(boolean countingPenaltyEnabled) {
        this.countingPenaltyEnabled = countingPenaltyEnabled;
        return this;
    }

    public Configuration setCountingChannelId(long countingChannelId) {
        this.countingChannelId = countingChannelId;
        return this;
    }
}
