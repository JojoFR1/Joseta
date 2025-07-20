package joseta.database.helper;

import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;
import arc.util.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;

import java.time.*;
import java.util.*;

public class MessagesDatabaseHelper {

    public static void populateNewGuild(Guild guild) {
        int count = 0;
        Log.debug("Populating the Messages Database...");
        for (TextChannel channel : guild.getTextChannels()) {
            for (ThreadChannel thread : channel.getThreadChannels()) count += addChannelMessageHistory(thread, guild);
            count += addChannelMessageHistory(channel, guild);
        }

        Log.debug("Populated Markov Messages Database with "+ count +" messages for guild: " + guild.getName() + " (" + guild.getId() + ")");
    }

    private static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild) {
        int count = 0;
        try {
            for (Message message : channel.getIterableHistory().stream().toList()) {
                User author = message.getAuthor();
                String content = message.getContentRaw();
                String timestamp = message.getTimeCreated().toInstant().toString();

                addNewMessage(message, guild, channel, author.getIdLong(), content, timestamp);
                count++;
            }
        } catch (Exception e) {
            Log.err("Could not populate the Messages database.", e);
        }

        return count;
    }

    public static void addNewMessage(Message message, Guild guild, GuildMessageChannel channel, long authorId, String content, String timestamp) {
        long id = message.getIdLong();
        long guildId = guild.getIdLong();
        long channelId = channel.getIdLong();
        
        Database.createOrUpdate(
            new MessageEntry(
                id,
                guildId,
                channelId,
                authorId,
                content,
                Instant.parse(timestamp)
            )
        );
    }

    public static void updateMessage(long messageId, long guildId, long channelId, String content) {
        MessageEntry entry = Database.querySelect(MessageEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(MessageEntry_.messageId), messageId),
                    cb.equal(rt.get(MessageEntry_.guildId), guildId),
                    cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).getResultList().get(0);
        
        Database.createOrUpdate(entry.setContent(content));
    }

    public static void deleteMessage(long messageId, long guildId, long channelId) {
        Database.queryDelete(MessageEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(MessageEntry_.messageId), messageId),
                    cb.equal(rt.get(MessageEntry_.guildId), guildId),
                    cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).executeUpdate();
    }

    public static void deleteChannelMessages(long guildId, long channelId) {
        Database.queryDelete(MessageEntry.class, (cb, rt) ->
                cb.and(cb.equal(rt.get(MessageEntry_.guildId), guildId),
                        cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).executeUpdate();
    }

    public static MessageEntry getMessageEntry(long messageId, long guildId, long channelId) {
        MessageEntry entry = Database.querySelect(MessageEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(MessageEntry_.messageId), messageId),
                    cb.equal(rt.get(MessageEntry_.guildId), guildId),
                    cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).getResultList().get(0);
        
        return entry;
    }

    public static Seq<MessageEntry> getMessageEntries(long guildId) {
        List<MessageEntry> entries = Database.querySelect(MessageEntry.class, (cb, rt) ->
                cb.equal(rt.get(MessageEntry_.guildId), guildId)
        ).getResultList();

        return Seq.with(entries);
    }
}