package dev.jojofr.joseta.database.entities;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity @Table(name = "configurations")
public class ConfigurationEntity {
    @Id public Long guildId;

    @Column public boolean welcomeEnabled = false;
    @Column public Long welcomeChannelId = null;
    @Column public boolean welcomeImageEnabled = false;
    @Column public String welcomeJoinMessage = "Bienvenue {{user}} !";
    @Column public String welcomeLeaveMessage = "**{{userName}}** nous a quitté...\n\n-# Si vous ne voulez pas que cela se reproduise, abonnez-vous à **__Joseta™ Premium 👑__** pour bénéficier de fonctionnalités exclusives et gardez vos membres engagés !";
    @Column public Long joinRoleId = null;
    @Column public Long joinBotRoleId = null;
    @Column public Long verifiedRoleId = null;
    
    @Column public boolean markovEnabled = false;
    @Column @ElementCollection(fetch = FetchType.EAGER) public Set<Long> markovBlacklist = new HashSet<>();
    
    @Column public boolean moderationEnabled = true;
    @Column(columnDefinition = "TEXT") public String rules = "";

    @Column public boolean autoResponseEnabled = false;

    @Column public boolean countingEnabled = false;
    @Column public boolean countingCommentsEnabled = false;
    @Column public boolean countingPenaltyEnabled = false;
    @Column public Long countingChannelId = null;
    
    // A non-private and no-arg constructor is required by JPA
    protected ConfigurationEntity() {}

    public ConfigurationEntity(long guildId) { this.guildId = guildId; }

    public ConfigurationEntity(ConfigurationEntity other) {
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
    
    public ConfigurationEntity setJoinBotRoleId(Long joinBotRoleId) {
        if (joinBotRoleId != null) this.joinBotRoleId = joinBotRoleId;
        return this;
    }

    public ConfigurationEntity setJoinBotRole(Role joinBotRole) {
        if (joinBotRole != null) this.joinBotRoleId = joinBotRole.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setJoinRoleId(Long joinRoleId) {
        if (joinRoleId != null) this.joinRoleId = joinRoleId;
        return this;
    }
    
    public ConfigurationEntity setVerifiedRole(Role verifiedRole) {
        if (verifiedRole != null) this.verifiedRoleId = verifiedRole.getIdLong();
        return this;
    }
    
    public ConfigurationEntity setVerifiedRoleId(Long verifiedRoleId) {
        if (verifiedRoleId != null) this.verifiedRoleId = verifiedRoleId;
        return this;
    }

    public ConfigurationEntity setMarkovEnabled(boolean markovEnabled) {
        this.markovEnabled = markovEnabled;
        return this;
    }

    public ConfigurationEntity setMarkovBlacklist(Set<Long> markovBlackList) {
        if (markovBlackList != null) this.markovBlacklist = markovBlackList;
        return this;
    }
    
    public ConfigurationEntity addMarkovBlacklist(IMentionable mentionable) {
        if (mentionable != null) this.markovBlacklist.add(mentionable.getIdLong());
        return this;
    }
    
    public ConfigurationEntity addIdMarkovBlacklist(Long id) {
        if (id != null) this.markovBlacklist.add(id);
        return this;
    }
    
    public ConfigurationEntity removeMarkovBlacklist(IMentionable mentionable) {
        if (mentionable != null) this.markovBlacklist.remove(mentionable.getIdLong());
        return this;
    }
    
    public ConfigurationEntity removeIdMarkovBlacklist(Long id) {
        if (id != null) this.markovBlacklist.remove(id);
        return this;
    }

    public ConfigurationEntity setModerationEnabled(boolean moderationEnabled) {
        this.moderationEnabled = moderationEnabled;
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
