package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import com.j256.ormlite.stmt.*;

public class MessagesDatabaseHelper {

    public static void populateNewGuild(Guild guild) {
        int count = 0;
        JosetaBot.logger.debug("Populating the Messages Database...");
        for (TextChannel channel : guild.getTextChannels()) {
            for (ThreadChannel thread : channel.getThreadChannels()) count += addChannelMessageHistory(thread, guild);
            count += addChannelMessageHistory(channel, guild);
        }

        JosetaBot.logger.debug("Populated Markov Messages Database with "+ count +" messages for guild: " + guild.getName() + " (" + guild.getId() + ")");
    }

    private static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild) {
        int count = 0;
        try {
            // TODO alternative for the hardcoded 10000 limit?
            for (Message message : channel.getIterableHistory().takeAsync(10000).thenApply(list -> list.stream().collect(Collectors.toList())).get()) {                
                User author = message.getAuthor();
                String content = message.getContentRaw();
                String timestamp = message.getTimeCreated().toInstant().toString();

                addNewMessage(message, guild, channel, author.getIdLong(), content, timestamp);
                count++;
            }
        } catch (Exception e) {
            JosetaBot.logger.error("Could not populate the Messages database.", e);
        }

        return count;
    }

    public static void addNewMessage(Message message, Guild guild, GuildMessageChannel channel, long authorId, String content, String timestamp) {
        long id = message.getIdLong();
        long guildId = guild.getIdLong();
        long channelId = channel.getIdLong();
        
        try {
            Databases.getInstance().getMessageDao().createIfNotExists(
                new MessageEntry(
                    id,
                    guildId,
                    channelId,
                    authorId,
                    content,
                    Instant.parse(timestamp)
                )
            );
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add a new message to the Markov database.", e);
        }
    }

    public static void updateMessage(long id, long guildId, long channelId, String content) {
        try {
            Databases databases = Databases.getInstance();
            MessageEntry entry = databases.getMessageDao().queryBuilder()
                .where()
                .eq("id", id)
                .and()
                .eq("guildId", guildId)
                .and()
                .eq("channelId", channelId)
                .queryForFirst();
            
            databases.getMessageDao().update(entry.setContent(content));
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update a message.", e);
        }
    }

    public static void deleteMessage(long id, long guildId, long channelId) {
        try {
            DeleteBuilder<MessageEntry, Long> deleteBuilder = Databases.getInstance().getMessageDao().deleteBuilder();
            
            deleteBuilder.where()
                .eq("id", id)
                .and()
                .eq("guildId", guildId)
                .and()
                .eq("channelId", channelId);

            deleteBuilder.delete();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete a message.", e);
        }
    }

    public static void deleteChannelMessages(long guildId, long channelId) {
        try {
            DeleteBuilder<MessageEntry, Long> deleteBuilder = Databases.getInstance().getMessageDao().deleteBuilder();
            
            deleteBuilder.where()
                .eq("guildId", guildId)
                .and()
                .eq("channelId", channelId);

            deleteBuilder.delete();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete a message.", e);
        }
    }

    public static MessageEntry getMessageEntry(long id, long guildId, long channelId) {
        MessageEntry entry;
        try {
            entry = Databases.getInstance().getMessageDao().queryBuilder()
                .where()
                .eq("id", id)
                .and()
                .eq("guildId", guildId)
                .and()
                .eq("channelId", channelId)
                .queryForFirst();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not retrieve message entry.", e);
            return null;
        }

        return entry;
    }

    public static Seq<MessageEntry> getMessageEntries(long guildId) {
        List<MessageEntry> entry;
        try {
            entry = Databases.getInstance().getMessageDao().queryForEq("guildId", guildId);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not retrieve message entries for guild: " + guildId, e);
            return null;
        }

        return Seq.with(entry);
    }
}