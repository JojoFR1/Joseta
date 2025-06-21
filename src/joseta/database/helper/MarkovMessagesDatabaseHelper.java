package joseta.database.helper;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;

import java.sql.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import com.j256.ormlite.stmt.*;

/** Copy of {@link MarkovMessagesHelper} but for the special Markov messages */
public class MarkovMessagesDatabaseHelper {
    private static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    private static final Pattern CLEAN_COPY_PATTERN = Pattern.compile("[^a-z0-9.?!,;\\-()~\"'&$€£ \\]\\[àáâãäåæçèéêëìíîïñòóôõöùúûüýÿ ]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPACED_PATTERN = Pattern.compile("\\s+");
    
    public static void populateNewGuild(Guild guild) {
        int count = 0;
        JosetaBot.logger.debug("Populating the Messages Database...");

        ConfigEntry config = ConfigDatabase.getConfig(guild.getIdLong());
        for (TextChannel channel : guild.getTextChannels()) {
            Seq<Long> markovBlackList = config.getMarkovBlackList();
            if (channel.isNSFW()
                || markovBlackList.contains(channel.getIdLong())
                || markovBlackList.contains(channel.getParentCategoryIdLong())
            ) continue;

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
                addNewMessage(message, guild, channel, message.getContentRaw());
                count++;
            }
        } catch (Exception e) {
            JosetaBot.logger.error("Could not populate the Messages database.", e);
        }

        return count;
    }

    public static void addNewMessage(Message message, Guild guild, GuildMessageChannel channel, String content) {
        long id = message.getIdLong();
        long guildId = guild.getIdLong();

        ConfigEntry config = ConfigDatabase.getConfig(guildId);
        Seq<Long> markovBlackList = config.getMarkovBlackList();
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return;
        if (markovBlackList.contains(channel.getIdLong())
            || markovBlackList.contains(message.getAuthor().getIdLong())
            || markovBlackList.containsAll(Seq.with(message.getMember().getRoles()).map(role -> role.getIdLong()))) return;
        
        if (channel instanceof TextChannel textChannel &&
            (textChannel.isNSFW() || markovBlackList.contains(textChannel.getParentCategoryIdLong()))) return;
        
        try {
            Databases.getInstance().getMarkovMessageDao().createIfNotExists(
                new MarkovMessageEntry(
                    id,
                    Databases.getInstance().getMessageDao().queryForId(id),
                    cleanMessage(content)
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
            
            databases.getMessageDao().update(entry.setContent(cleanMessage(content)));
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

    private static String cleanMessage(String string) {
        String lower = string.toLowerCase().replace('\n', ' ').trim();
        String noUrl = NO_URL_PATTERN.matcher(lower).replaceAll("");
        String cleanCopy = CLEAN_COPY_PATTERN.matcher(noUrl).replaceAll("").replace('.', ' ');
        String spaced = SPACED_PATTERN.matcher(cleanCopy).replaceAll(" ");

        return spaced;
    }
}