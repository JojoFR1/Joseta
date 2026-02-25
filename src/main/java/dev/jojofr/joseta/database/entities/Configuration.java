package dev.jojofr.joseta.database.entities;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "configurations")
public class Configuration {
    @Id public long guildId;

    @Column public boolean welcomeEnabled = false;
    @Column public Long welcomeChannelId = null;
    @Column public boolean welcomeImageEnabled = false;
    @Column public String welcomeJoinMessage = "Bienvenue {{user}} !";
    @Column public String welcomeLeaveMessage = "**{{userName}}** nous a quitté...";
    @Column public Long joinRoleId = null;
    @Column public Long joinBotRoleId = null;
    @Column public Long verifiedRoleId = null;
    
    @Column public boolean markovEnabled = false;
    @Column @ElementCollection(fetch = FetchType.EAGER) public Set<Long> markovBlacklist = new HashSet<>();
    
    @Column public boolean moderationEnabled = true;
    @Column public boolean moderationLogsEnabled = false;
    @Column(columnDefinition = "TEXT") public String rules = "";
    
    @Column public boolean autoResponseEnabled = false;

    @Column public boolean countingEnabled = false;
    @Column public boolean countingCommentsEnabled = false;
    @Column public boolean countingPenaltyEnabled = false;
    @Column public Long countingChannelId = null;
    
    // A non-private and no-arg constructor is required by JPA
    protected Configuration() {}

    public Configuration(long guildId) { this.guildId = guildId; }
    
    
    public Configuration setGuildId(Long guildId) {
        if (guildId != null) this.guildId = guildId;
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

    public Configuration setMarkovEnabled(boolean markovEnabled) {
        this.markovEnabled = markovEnabled;
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

    public Configuration setModerationEnabled(boolean moderationEnabled) {
        this.moderationEnabled = moderationEnabled;
        return this;
    }
    
    public Configuration setRules(String rules) {
        if (rules != null) this.rules = rules;
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
