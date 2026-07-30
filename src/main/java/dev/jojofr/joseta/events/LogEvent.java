package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.annotations.types.EventPriority;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.awt.*;
import java.time.Instant;

@EventModule
public class LogEvent {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void messageUpdateEvent(MessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
      
        MessageEntity oldMessage = Database.withExtension(MessageDao.class, dao -> dao.getById(event.getMessageIdLong()));
        if (oldMessage == null) return; // Message not found in database, cannot log
        
        // Old content is retrieved from database before it gets updated
       guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
           buildEmbed(event.getGuild(), event.getAuthor(),
               Color.ORANGE,
               "**Message envoyé par <@" + event.getAuthor().getId() + "> modifié (" + event.getMessage().getJumpUrl() + ")**\n\n" +
                   "**Ancien**\n```" + oldMessage.content + "```\n" +
                   "**Nouveau**\n```" + event.getMessage().getContentRaw() + "```"
           )).queue();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void messageDeleteEvent(MessageDeleteEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        MessageEntity oldMessage = Database.withExtension(MessageDao.class, dao -> dao.getById(event.getMessageIdLong()));
        if (oldMessage == null) return; // Message not found in database, cannot log
        
        User user = event.getJDA().getUserById(oldMessage.authorId);
        
        guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
            buildEmbed(event.getGuild(), user,
                Color.RED,
                "**Message envoyé par <@" + oldMessage.authorId + "> dans <#" + oldMessage.channelId +"> supprimé**\n\n" +
                "**Contenu**\n```" + oldMessage.content + "```"
            )).queue();
    }
    
    // TODO check if moderator moved
    @EventHandler(priority = EventPriority.HIGH)
    public void voiceUpdateEvent(GuildVoiceUpdateEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();
        MessageEmbed embed = null;
        
        // Moved between voice channels
        if (joinedChannel != null && leftChannel != null) {
            embed = buildEmbed(event.getGuild(), event.getMember().getUser(),
                Color.decode("#4A91E2"),
                "**<@" + event.getMember().getId() + "> a changé de salon vocal de <#" + leftChannel.getId() + "> vers <#" + joinedChannel.getId() + ">**"
            );
        }
        // Joined a voice channel
        else if (joinedChannel != null) {
            embed = buildEmbed(event.getGuild(), event.getMember().getUser(),
                Color.decode("#4A91E2"),
                "**<@" + event.getMember().getId() + "> a rejoint le salon vocal <#" + joinedChannel.getId() + ">**"
            );
        }
        // Left a voice channel
        else if (leftChannel != null) {
            embed = buildEmbed(event.getGuild(), event.getMember().getUser(),
                Color.decode("#4A91E2"),
                "**<@" + event.getMember().getId() + "> a quitté le salon vocal <#" + leftChannel.getId() + ">**"
            );
        }
        
        guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(embed).queue();
    }
    
    private MessageEmbed buildEmbed(Guild guild, User user, Color color, String description) {
        return new EmbedBuilder()
            .setColor(color)
            .setAuthor(user != null ? user.getName() : "Utilisateur inconnu", null, user != null ? user.getEffectiveAvatarUrl() : null)
            .setDescription(description)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now())
            .build();
    }
    
    private boolean checkLogEnabled(Guild guild) {
        GuildConfiguration guildConfiguration = BotCache.getGuildConfiguration(guild.getIdLong());
        if (guildConfiguration.configuration == null || !guildConfiguration.configuration.moderationLogEnabled) return false;
        
        if (guildConfiguration.getModerationLogChannel(guild) == null) return false;
        
        return true;
    }
}
