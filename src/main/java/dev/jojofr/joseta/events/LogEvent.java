package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.awt.*;
import java.time.Instant;

@EventModule
public class LogEvent {
    
    @EventHandler(type = EventType.MESSAGE_UPDATE, priority = EventHandler.EventPriority.HIGH)
    public void messageUpdateEvent(MessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        
        ConfigurationEntity configuration = BotCache.getGuildConfiguration(event.getGuild().getIdLong());
        if (configuration == null || !configuration.moderationLogsEnabled) return;
        
        TextChannel channel;
        if (configuration.moderationLogsChannelId == null || (channel = event.getGuild().getTextChannelById(configuration.moderationLogsChannelId)) == null) return;
        
        MessageEntity oldMessage = Database.get(MessageEntity.class, event.getMessageIdLong());
        if (oldMessage == null) return; // Message not found in database, cannot log
        
        MessageEmbed embed = new EmbedBuilder()
            .setColor(Color.ORANGE)
            .setAuthor(event.getAuthor().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
            .setDescription("**Message modifié: " + event.getMessage().getJumpUrl() + "**\n\n" +
                "**Avant**\n```" + oldMessage.content + "```\n" +
                "**Après**\n```" + event.getMessage().getContentRaw() + "```")
            .setFooter(event.getGuild().getName(), event.getGuild().getIconUrl())
            .setTimestamp(Instant.now())
            .build();
        
        // Old content is retrieved from database before it gets updated
        channel.sendMessageEmbeds(embed).queue();
        
    }
}
