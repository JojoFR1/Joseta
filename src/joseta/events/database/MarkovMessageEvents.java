package joseta.events.database;

import joseta.database.*;

import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.message.*;

public class MarkovMessageEvents {

    public static void executeMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs
        
        long authorId = event.getAuthor().getIdLong();
        String content = event.getMessage().getContentRaw();
        String timestamp = event.getMessage().getTimeCreated().toString();

        MarkovMessagesDatabase.addNewMessage(event.getMessage(), event.getGuild(), event.getChannel().asGuildMessageChannel(), authorId, content, timestamp);
    }

    public static void executeMessageUpdate(MessageUpdateEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs
        
        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        String content = event.getMessage().getContentRaw();

        MarkovMessagesDatabase.updateMessage(id, guildId, channelId, content);
    }

    public static void executeMessageDelete(MessageDeleteEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs

        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        MarkovMessagesDatabase.deleteMessage(id, guildId, channelId);
    }

    public static void executeMessageBulkDelete(MessageBulkDeleteEvent event) {
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        for (String id : event.getMessageIds()) {
            MarkovMessagesDatabase.deleteMessage(Long.parseLong(id), guildId, channelId);
        }
    }
    
    public static void executeChannelDelete(ChannelDeleteEvent event) {
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        MarkovMessagesDatabase.deleteChannelMessages(guildId, channelId);
    }

}
