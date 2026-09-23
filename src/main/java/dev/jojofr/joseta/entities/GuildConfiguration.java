package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class GuildConfiguration {
    public final ConfigurationEntity configuration;
    public Set<Long> markovBlacklistIds = new HashSet<>();
    
    public int totalMessages = 0;
    public long totalVoiceTime = 0;
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
        if (guild.getIconUrl() == null) { accentColor = Color.GRAY; return; }
        
        try (InputStream is = new URI(guild.getIcon().getUrl(64)).toURL().openStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) { accentColor = Color.GRAY; return; }
            
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            if (pixels == null || pixels.length == 0) { accentColor = Color.GRAY; return; }

            int pixelCount = pixels.length;
            
            long r = 0, g = 0, b = 0;
            for (int argb : pixels) {
                if (((argb >> 24) & 0xFF) < 16) continue;
                
                r += (argb >> 16) & 0xFF;
                g += (argb >> 8) & 0xFF;
                b += argb & 0xFF;
            }
            
            accentColor = new Color((int) (r / pixelCount), (int) (g / pixelCount), (int) (b / pixelCount));
        } catch (Exception e) {
            Log.err("Failed to update accent color for guild: {} (ID: {})", e, guild.getName(), guild.getIdLong());
            accentColor = Color.GRAY;
        }
    }
    
    public void updateStats() {
        Database.useHandle(handle -> {
            totalMessages = handle.attach(MessageDao.class).getGuildMessageCount(configuration.guildId);
            totalVoiceTime = handle.attach(UserDao.class).getTotalTimeVoice(configuration.guildId);
        });
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
