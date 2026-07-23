package dev.jojofr.joseta.database.entities;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationEntity {
    public long guildId;
    
    public boolean welcomeEnabled = false;
    public boolean welcomeImageEnabled = false;
    public Long welcomeChannelId;
    public String welcomeJoinMessage = "Bienvenue {{user}} !";
    public String welcomeLeaveMessage= "**{{userName}}** nous a quitté...";
    public Long joinRoleId;
    public Long joinRoleBotId;
    public Long roleVerifiedId;
    
    public boolean markovEnabled = false;
    public Set<Long> markovBlacklistIds = new HashSet<>();
    
    public boolean moderationEnabled = true;
    public boolean moderationLogEnabled = false;
    public Long moderationLogChannelId;
    public boolean moderationHoneypotEnabled = false;
    public Long moderationHoneypotChannelId;
    public String rules = "";
    
     public boolean autoResponseEnabled = false;
    
     public boolean countingEnabled = false;
     public boolean countingCommentsEnabled = false;
     public boolean countingPenaltyEnabled = false;
     public Long countingChannelId;
    
    // A non-private and no-arg constructor is required by JDBI
    protected ConfigurationEntity() {}
    public ConfigurationEntity(long guildId) { this.guildId = guildId; }
    public ConfigurationEntity(ConfigurationEntity other) {
        this.guildId = other.guildId;
        this.welcomeEnabled = other.welcomeEnabled;
        this.welcomeImageEnabled = other.welcomeImageEnabled;
        this.welcomeChannelId = other.welcomeChannelId;
        this.welcomeJoinMessage = other.welcomeJoinMessage;
        this.welcomeLeaveMessage = other.welcomeLeaveMessage;
        this.joinRoleId = other.joinRoleId;
        this.joinRoleBotId = other.joinRoleBotId;
        this.roleVerifiedId = other.roleVerifiedId;
        this.markovEnabled = other.markovEnabled;
        this.markovBlacklistIds = new HashSet<>(other.markovBlacklistIds);
        this.moderationEnabled = other.moderationEnabled;
        this.moderationLogEnabled = other.moderationLogEnabled;
        this.moderationLogChannelId = other.moderationLogChannelId;
        this.moderationHoneypotEnabled = other.moderationHoneypotEnabled;
        this.moderationHoneypotChannelId = other.moderationHoneypotChannelId;
        this.rules = other.rules;
        this.autoResponseEnabled = other.autoResponseEnabled;
        this.countingEnabled = other.countingEnabled;
        this.countingCommentsEnabled = other.countingCommentsEnabled;
        this.countingPenaltyEnabled = other.countingPenaltyEnabled;
        this.countingChannelId = other.countingChannelId;
    }
    
    public ConfigurationEntity setGuildId(Long guildId) {
        if (guildId != null) this.guildId = guildId;
        return this;
    }
    
    public ConfigurationEntity setWelcomeEnabled(boolean welcomeEnabled) {
        this.welcomeEnabled = welcomeEnabled;
        return this;
    }
    
    public ConfigurationEntity setWelcomeImageEnabled(boolean welcomeImageEnabled) {
        this.welcomeImageEnabled = welcomeImageEnabled;
        return this;
    }
    
    public ConfigurationEntity setWelcomeChannel(GuildMessageChannel welcomeChannel) {
        if (welcomeChannel != null) this.welcomeChannelId = welcomeChannel.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setWelcomeChannelId(Long welcomeChannelId) {
        if (welcomeChannelId != null) this.welcomeChannelId = welcomeChannelId;
        return this;
    }
    
    public ConfigurationEntity setWelcomeJoinMessage(String welcomeJoinMessage) {
        if (welcomeJoinMessage != null) this.welcomeJoinMessage = welcomeJoinMessage;
        return this;
    }
    
    public ConfigurationEntity setWelcomeLeaveMessage(String welcomeLeaveMessage) {
        if (welcomeLeaveMessage != null) this.welcomeLeaveMessage = welcomeLeaveMessage;
        return this;
    }
    
    public ConfigurationEntity setJoinRole(Role joinRole) {
        if (joinRole != null) this.joinRoleId = joinRole.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setJoinRoleBotId(Long joinRoleBotId) {
        if (joinRoleBotId != null) this.joinRoleBotId = joinRoleBotId;
        return this;
    }
    
    public ConfigurationEntity setJoinBotRole(Role joinBotRole) {
        if (joinBotRole != null) this.joinRoleBotId = joinBotRole.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setJoinRoleId(Long joinRoleId) {
        if (joinRoleId != null) this.joinRoleId = joinRoleId;
        return this;
    }
    
    public ConfigurationEntity setVerifiedRole(Role verifiedRole) {
        if (verifiedRole != null) this.roleVerifiedId = verifiedRole.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setRoleVerifiedId(Long roleVerifiedId) {
        if (roleVerifiedId != null) this.roleVerifiedId = roleVerifiedId;
        return this;
    }
    
    public ConfigurationEntity setMarkovEnabled(boolean markovEnabled) {
        this.markovEnabled = markovEnabled;
        return this;
    }
    
    public ConfigurationEntity setModerationEnabled(boolean moderationEnabled) {
        this.moderationEnabled = moderationEnabled;
        return this;
    }
    
    public ConfigurationEntity setModerationLogEnabled(boolean moderationLogsEnabled) {
        this.moderationLogEnabled = moderationLogsEnabled;
        return this;
    }
    
    public ConfigurationEntity setModerationLogChannel(GuildMessageChannel moderationLogsChannel) {
        if (moderationLogsChannel != null) this.moderationLogChannelId = moderationLogsChannel.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setModerationLogChannelId(Long moderationLogsChannelId) {
        if (moderationLogsChannelId != null) this.moderationLogChannelId = moderationLogsChannelId;
        return this;
    }
    
    public ConfigurationEntity setModerationHoneypotEnabled(boolean moderationHoneypotEnabled) {
        this.moderationHoneypotEnabled = moderationHoneypotEnabled;
        return this;
    }
    
    public ConfigurationEntity setModerationHoneypotChannel(GuildMessageChannel moderationHoneypotChannel) {
        if (moderationHoneypotChannel != null) this.moderationHoneypotChannelId = moderationHoneypotChannel.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setModerationHoneypotChannelId(Long moderationHoneypotChannelId) {
        if (moderationHoneypotChannelId != null) this.moderationHoneypotChannelId = moderationHoneypotChannelId;
        return this;
    }
    
    public ConfigurationEntity setRules(String rules) {
        if (rules != null) this.rules = rules;
        return this;
    }
    
    public ConfigurationEntity setAutoResponseEnabled(boolean autoResponseEnabled) {
        this.autoResponseEnabled = autoResponseEnabled;
        return this;
    }
    
    public ConfigurationEntity setCountingEnabled(boolean countingEnabled) {
        this.countingEnabled = countingEnabled;
        return this;
    }
    
    public ConfigurationEntity setCountingCommentsEnabled(boolean countingCommentsEnabled) {
        this.countingCommentsEnabled = countingCommentsEnabled;
        return this;
    }
    
    public ConfigurationEntity setCountingPenaltyEnabled(boolean countingPenaltyEnabled) {
        this.countingPenaltyEnabled = countingPenaltyEnabled;
        return this;
    }
    
    public ConfigurationEntity setCountingChannel(GuildMessageChannel messageChannel) {
        if (messageChannel != null) this.countingChannelId = messageChannel.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setCountingChannelId(Long countingChannelId) {
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
