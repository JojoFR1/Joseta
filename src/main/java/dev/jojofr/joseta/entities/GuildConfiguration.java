package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashSet;
import java.util.Set;

public class GuildConfiguration {
    public final ConfigurationEntity configuration;
    public Set<Long> markovBlacklistIds = new HashSet<>();
    
    public GuildConfiguration(ConfigurationEntity configuration) {
        this.configuration = configuration;
    }
    
    public GuildConfiguration(GuildConfiguration other) {
        this.configuration = new ConfigurationEntity(other.configuration);
        this.markovBlacklistIds = new HashSet<>(other.markovBlacklistIds);
    }
    
    public TextChannel getWelcomeChannel(Guild guild) {
        Long id = configuration.welcomeChannelId;
        return id == null ? null : guild.getTextChannelById(id);
    }
    
    public Role getJoinRole(Guild guild) {
        Long id = configuration.joinRoleId;
        return id == null ? null : guild.getRoleById(id);
    }
    
    public Role getJoinBotRole(Guild guild) {
        Long id = configuration.joinRoleBotId;
        return id == null ? null : guild.getRoleById(id);
    }
    
    public TextChannel getModerationLogChannel(Guild guild) {
        Long id = configuration.moderationLogChannelId;
        return id == null ? null : guild.getTextChannelById(id);
    }
}
