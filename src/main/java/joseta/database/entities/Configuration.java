package joseta.database.entities;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "configurations")
public class Configuration {
    @Id public Long guildId;

    @Column public Boolean welcomeEnabled = false;
    @Column public Long welcomeChannelId = null;
    @Column public Boolean welcomeImageEnabled = false;
    @Column public String welcomeJoinMessage = "Bienvenue {{user}} !";
    @Column public String welcomeLeaveMessage = "**{{userName}}** nous a quitté...";
    @Column public String welcomeImageUrl = ""; // Could switch to file upload using modal
    @Column public Long joinRoleId = null;
    @Column public Long joinBotRoleId = null;
    @Column public Long verifiedRoleId = null;
    
    @Column public Boolean markovEnabled = false;
    @Column @ElementCollection(fetch = FetchType.EAGER) public Set<Long> markovBlacklist = new HashSet<>();
    
    @Column public Boolean moderationEnabled = true;
    @Column public String rules = "";

    @Column public Boolean autoResponseEnabled = false;

    @Column public Boolean countingEnabled = false;
    @Column public Boolean countingCommentsEnabled = false;
    @Column public Boolean countingPenaltyEnabled = false;
    @Column public Long countingChannelId = null;
    
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
        this.markovBlacklist = other.markovBlacklist;

        this.moderationEnabled = other.moderationEnabled;

        this.autoResponseEnabled = other.autoResponseEnabled;

        this.countingEnabled = other.countingEnabled;
        this.countingCommentsEnabled = other.countingCommentsEnabled;
        this.countingPenaltyEnabled = other.countingPenaltyEnabled;
        this.countingChannelId = other.countingChannelId;
    }
    
    
    public Configuration setGuildId(Long guildId) {
        if (guildId != null) this.guildId = guildId;
        return this;
    }

    public Configuration setWelcomeEnabled(Boolean welcomeEnabled) {
        if (welcomeEnabled != null) this.welcomeEnabled = welcomeEnabled;
        return this;
    }

    public Configuration setWelcomeImageEnabled(Boolean welcomeImageEnabled) {
        if (welcomeImageEnabled != null) this.welcomeImageEnabled = welcomeImageEnabled;
        return this;
    }

    public Configuration setWelcomeChannel(GuildMessageChannel welcomeChannel) {
        if (welcomeChannel != null) this.welcomeChannelId = welcomeChannel.getIdLong();
        return this;
    }
    
    public Configuration setWelcomeChannelId(Long welcomeChannelId) {
        if (welcomeChannelId != null) this.welcomeChannelId = welcomeChannelId;
        return this;
    }

    public Configuration setWelcomeJoinMessage(String welcomeJoinMessage) {
        if (welcomeJoinMessage != null) this.welcomeJoinMessage = welcomeJoinMessage;
        return this;
    }

    public Configuration setWelcomeLeaveMessage(String welcomeLeaveMessage) {
        if (welcomeLeaveMessage != null) this.welcomeLeaveMessage = welcomeLeaveMessage;
        return this;
    }
    
    public Configuration setWelcomeImageUrl(String welcomeImageUrl) {
        if (welcomeImageUrl != null) this.welcomeImageUrl = welcomeImageUrl;
        return this;
    }

    public Configuration setJoinRole(Role joinRole) {
        if (joinRole != null) this.joinRoleId = joinRole.getIdLong();
        return this;
    }
    
    public Configuration setJoinBotRoleId(Long joinBotRoleId) {
        if (joinBotRoleId != null) this.joinBotRoleId = joinBotRoleId;
        return this;
    }

    public Configuration setJoinBotRole(Role joinBotRole) {
        if (joinBotRole != null) this.joinBotRoleId = joinBotRole.getIdLong();
        return this;
    }
    
    public Configuration setJoinRoleId(Long joinRoleId) {
        if (joinRoleId != null) this.joinRoleId = joinRoleId;
        return this;
    }
    
    public Configuration setVerifiedRole(Role verifiedRole) {
        if (verifiedRole != null) this.verifiedRoleId = verifiedRole.getIdLong();
        return this;
    }
    
    public Configuration setVerifiedRoleId(Long verifiedRoleId) {
        if (verifiedRoleId != null) this.verifiedRoleId = verifiedRoleId;
        return this;
    }

    public Configuration setMarkovEnabled(Boolean markovEnabled) {
        if (markovEnabled != null) this.markovEnabled = markovEnabled;
        return this;
    }

    public Configuration setMarkovBlacklist(Set<Long> markovBlackList) {
        if (markovBlackList != null) this.markovBlacklist = markovBlackList;
        return this;
    }
    
    public Configuration addMarkovBlacklist(IMentionable mentionable) {
        if (mentionable != null) this.markovBlacklist.add(mentionable.getIdLong());
        return this;
    }
    
    public Configuration addIdMarkovBlacklist(Long id) {
        if (id != null) this.markovBlacklist.add(id);
        return this;
    }
    
    public Configuration removeMarkovBlacklist(IMentionable mentionable) {
        if (mentionable != null) this.markovBlacklist.remove(mentionable.getIdLong());
        return this;
    }
    
    public Configuration removeIdMarkovBlacklist(Long id) {
        if (id != null) this.markovBlacklist.remove(id);
        return this;
    }

    public Configuration setModerationEnabled(Boolean moderationEnabled) {
        if (moderationEnabled != null) this.moderationEnabled = moderationEnabled;
        return this;
    }
    
    public Configuration setRules(String rules) {
        if (rules != null) this.rules = rules;
        return this;
    }
    
    public Configuration setAutoResponseEnabled(Boolean autoResponseEnabled) {
        if (autoResponseEnabled != null) this.autoResponseEnabled = autoResponseEnabled;
        return this;
    }

    public Configuration setCountingEnabled(Boolean countingEnabled) {
        if (countingEnabled != null) this.countingEnabled = countingEnabled;
        return this;
    }

    public Configuration setCountingCommentsEnabled(Boolean countingCommentsEnabled) {
        if (countingCommentsEnabled != null) this.countingCommentsEnabled = countingCommentsEnabled;
        return this;
    }

    public Configuration setCountingPenaltyEnabled(Boolean countingPenaltyEnabled) {
        if (countingPenaltyEnabled != null) this.countingPenaltyEnabled = countingPenaltyEnabled;
        return this;
    }
    
    public Configuration setCountingChannel(GuildMessageChannel messageChannel) {
        if (messageChannel != null) this.countingChannelId = messageChannel.getIdLong();
        return this;
    }
    
    public Configuration setCountingChannelId(Long countingChannelId) {
        if (countingChannelId != null) this.countingChannelId = countingChannelId;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Configuration{");
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                sb.append(field.getName()).append("=").append(field.get(this)).append(", ");
            } catch (IllegalAccessException e) {
                sb.append(field.getName()).append("=ACCESS_ERROR, ");
            } catch (NullPointerException e) {
                sb.append(field.getName()).append("=NULL, ");
            }
        }
        if (fields.length > 0) sb.setLength(sb.length() - 2); // Remove last comma and space
        sb.append("}");
        return sb.toString();
    }
}
