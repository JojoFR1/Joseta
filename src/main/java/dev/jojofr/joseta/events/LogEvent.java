package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.Event;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.database.entities.Message;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.BotCache;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@EventModule
public class LogEvent {
    
    //TODO Need some kind of priority system to make sure this runs before the MessageDatabase update
    @Event(type = EventType.MESSAGE_UPDATE)
    public void messageUpdateEvent(MessageUpdateEvent event) {
        Configuration configuration = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
        if (configuration == null || !configuration.moderationLogsEnabled) return;
        
        TextChannel channel;
        if (configuration.moderationLogsChannelId == null || (channel = event.getGuild().getTextChannelById(configuration.moderationLogsChannelId)) == null) return;
        
        // Old content is retrieved from database before it gets updated
        channel.sendMessage("**Message édité**\n" +
            "Auteur : "+ event.getAuthor().getAsTag() + " ("+ event.getAuthor().getId() +")\n" +
            "Salon : "+ event.getChannel().getName() +" ("+ event.getChannel().getId() +")\n" +
            "Ancien message : "+ Database.get(Message.class, event.getMessageIdLong()).content + "\n" +
            "Nouveau message : "+ event.getMessage().getContentDisplay()
        ).queue();
        
    }
}
