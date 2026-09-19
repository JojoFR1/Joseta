package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class GuildConfiguration {
    public final ConfigurationEntity configuration;
    public Set<Long> markovBlacklistIds = new HashSet<>();
    
    public int totalMessages = 0;
    public int totalVoiceTime = 0;
    public Color accentColor = null;
    
    public GuildConfiguration(ConfigurationEntity configuration) {
        this.configuration = configuration;
    }
    
    public GuildConfiguration(GuildConfiguration other) {
        this.configuration = new ConfigurationEntity(other.configuration);
        this.markovBlacklistIds = new HashSet<>(other.markovBlacklistIds);
        
        this.totalMessages = other.totalMessages;
        this.totalVoiceTime = other.totalVoiceTime;
        this.accentColor = other.accentColor;
    }
    
    public void updateAccentColor(Guild guild) {
        if (guild.getIconUrl() == null) { accentColor = null; return; }
        
        try {
            BufferedImage guildIcon = ImageIO.read(new URI(guild.getIconUrl()).toURL());
            if (guildIcon == null) { accentColor = null; return; }
            
            long red = 0, green = 0, blue = 0, count = 0;
            
            for (int i = 0; i < guildIcon.getWidth() * guildIcon.getHeight(); i++) {
                int rgb = guildIcon.getRGB(i % guildIcon.getWidth(), i / guildIcon.getWidth());
                Color color = new Color(rgb, true);
                
                if (color.getAlpha() < 128) continue;
                red += color.getRed(); green += color.getGreen(); blue += color.getBlue(); count++;
            }
            
            if (count == 0) { accentColor = null; return; }
            
            accentColor = new Color((int) (red / count), (int) (green / count), (int) (blue / count));
        } catch (Exception e) {
            Log.err("Failed to update accent color for guild: {} (ID: {})", e, guild.getName(), guild.getIdLong());
            accentColor = null;
        }
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
}
