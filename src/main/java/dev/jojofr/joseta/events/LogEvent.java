package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.annotations.types.EventPriority;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.entities.GuildConfiguration;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.internal.entities.GuildImpl;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;

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
                "**<@" + event.getMember().getId() + "> a été déplacé par <@" + event.getMember().getId() + "> de <#" + leftChannel.getId() + "> vers <#" + joinedChannel.getId() + ">**"
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void channelCreateEvent(ChannelCreateEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).queue(logs -> {
            AuditLogEntry log = logs.getFirst();
            User user = log != null ? log.getUser() : null;
            
            guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
                buildEmbed(event.getGuild(), user,
                    Color.decode("#71C11F"),
                    "**Salon <#" + event.getChannel().getId() + "> (`" + event.getChannel().getName() + "`) créé**"
                )).queue();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void channelUpdateEvent(GenericChannelUpdateEvent<?> event) {
    
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void channelDeleteEvent(ChannelDeleteEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_DELETE).queue(logs -> {
            AuditLogEntry log = logs.getFirst();
            User user = log != null ? log.getUser() : null;
            
            guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
                buildEmbed(event.getGuild(), user,
                    Color.decode("#C11F1F"),
                    "**Salon <#" + event.getChannel().getId() + "> (`" + event.getChannel().getName() + "`) supprimé**"
                )).queue();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void emojiAddedEvent(EmojiAddedEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        event.getGuild().retrieveAuditLogs().type(ActionType.EMOJI_CREATE).queue(logs -> {
            AuditLogEntry log = logs.getFirst();
            User user = log != null ? log.getUser() : null;
            
            guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
                buildEmbed(event.getGuild(), user,
                    Color.decode("#71C11F"),
                    "**Emoji " + event.getEmoji().getAsMention() + " (`" + event.getEmoji().getName() + "`) ajouté**"
                )).queue();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void emojiRemovedEvent(EmojiRemovedEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        event.getGuild().retrieveAuditLogs().type(ActionType.EMOJI_DELETE).queue(logs -> {
            AuditLogEntry log = logs.getFirst();
            User user = log != null ? log.getUser() : null;
            
            guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
                buildEmbed(event.getGuild(), user,
                    Color.decode("#C11F1F"),
                    "**Emoji " + event.getEmoji().getAsMention() + " (`" + event.getEmoji().getName() + "`) supprimé**"
                )).queue();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void emojiUpdateNameEvent(EmojiUpdateNameEvent event) {
        if (!checkLogEnabled(event.getGuild())) return;
        GuildConfiguration guildConfig = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        
        event.getGuild().retrieveAuditLogs().type(ActionType.EMOJI_UPDATE).queue(logs -> {
            AuditLogEntry log = logs.getFirst();
            User user = log != null ? log.getUser() : null;
            
            guildConfig.getModerationLogChannel(event.getGuild()).sendMessageEmbeds(
                buildEmbed(event.getGuild(), user,
                    Color.decode("#F8E61C"),
                    "**Emoji " + event.getEmoji().getAsMention() + " renommé de `" + event.getOldName() + "` à `" + event.getNewName() + "`**"
                )).queue();
        });
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
