package joseta.events.database;

import joseta.database.helper.*;

import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.message.*;

public class MessageEvents {

    public static void executeMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs
        
        long authorId = event.getAuthor().getIdLong();
        String content = event.getMessage().getContentRaw();
        String timestamp = event.getMessage().getTimeCreated().toString();

        MessagesDatabaseHelper.addNewMessage(event.getMessage(), event.getGuild(), event.getChannel().asGuildMessageChannel(), authorId, content, timestamp);
        MarkovMessagesDatabaseHelper.addNewMessage(event.getMessage(), event.getGuild(), event.getChannel().asGuildMessageChannel(), authorId, content, timestamp);
    }

    public static void executeMessageUpdate(MessageUpdateEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs
        
        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        String content = event.getMessage().getContentRaw();

        MessagesDatabaseHelper.updateMessage(id, guildId, channelId, content);
        MarkovMessagesDatabaseHelper.updateMessage(id, guildId, channelId, content);
    }

    public static void executeMessageDelete(MessageDeleteEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs

        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        MessagesDatabaseHelper.deleteMessage(id, guildId, channelId);
        MarkovMessagesDatabaseHelper.deleteMessage(id, guildId, channelId);
    }

    public static void executeMessageBulkDelete(MessageBulkDeleteEvent event) {
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        for (String id : event.getMessageIds()) {
            MessagesDatabaseHelper.deleteMessage(Long.parseLong(id), guildId, channelId);
            MarkovMessagesDatabaseHelper.deleteMessage(Long.parseLong(id), guildId, channelId);
        }
    }
    
    public static void executeChannelDelete(ChannelDeleteEvent event) {
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        MessagesDatabaseHelper.deleteChannelMessages(guildId, channelId);
        MarkovMessagesDatabaseHelper.deleteChannelMessages(guildId, channelId);
    }
}
