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
import java.util.regex.*;

public class MessagesDatabaseHelper {
    private static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    private static final Pattern CLEAN_COPY_PATTERN = Pattern.compile("[^a-z0-9.?!,;\\-()~\"'&$€£\\]\\[àáâãäåæçèéêëìíîïñòóôõöùúûüýÿ ]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPACED_PATTERN = Pattern.compile("\\s+");

    public static void populateNewGuild(Guild guild) {
        int count = 0;
        Log.debug("Populating the Messages Database...");

        for (TextChannel channel : guild.getTextChannels()) {
            for (ThreadChannel thread : channel.getThreadChannels()) count += addChannelMessageHistory(thread, guild);
            count += addChannelMessageHistory(channel, guild);
        }

        Log.debug("Populated Messages Database with "+ count +" messages for guild: " + guild.getName() + " (" + guild.getId() + ")");
    }

    private static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild) {
        int count = 0;
        try {
            for (Message message : channel.getIterableHistory().stream().toList()) {
                addNewMessage(message, guild, channel, message.getAuthor().getIdLong(), message.getContentRaw(), message.getTimeCreated().toInstant());
                count++;
            }
        } catch (Exception e) {
            Log.err("Could not populate the Messages database.", e);
        }

        return count;
    }

    public static void addNewMessage(Message message, Guild guild, GuildMessageChannel channel, long authorId, String content, Instant timestamp) {
        long id = message.getIdLong();
        long guildId = guild.getIdLong();
        long channelId = channel.getIdLong();
        String markovContent = null;

        ConfigEntry config = Database.get(ConfigEntry.class, guildId);

        // TODO maybe make this into multiple if statements for better readability
        Seq<Long> markovBlackList = config.getMarkovBlackList();
        if (!((message.getAuthor().isBot() || message.getAuthor().isSystem())
                && (message.getMember() != null && (markovBlackList.contains(message.getAuthor().getIdLong()) || markovBlackList.containsAll(Seq.with(message.getMember().getRoles()).map(role -> role.getIdLong()))))
                && (channel instanceof TextChannel textChannel && (textChannel.isNSFW() || markovBlackList.contains(textChannel.getParentCategoryIdLong())))
        )) markovContent = cleanMessage(content);

        Database.createOrUpdate(
            new MessageEntry(
                id,
                guildId,
                channelId,
                authorId,
                content,
                markovContent,
                timestamp
            )
        );
    }

    public static void updateMessage(long messageId, long guildId, long channelId, String content) {
        if (getMessageEntry(messageId, guildId, channelId) == null) return; // Entry does not exist, no need to update

        MessageEntry entry = Database.querySelect(MessageEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(MessageEntry_.messageId), messageId),
                    cb.equal(rt.get(MessageEntry_.guildId), guildId),
                    cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).getResultList().get(0);

        if (entry.getMarkovContent() != null) entry.setMarkovContent(cleanMessage(content));
        
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
        List<MessageEntry> entries = Database.querySelect(MessageEntry.class, (cb, rt) ->
            cb.and(cb.equal(rt.get(MessageEntry_.messageId), messageId),
                    cb.equal(rt.get(MessageEntry_.guildId), guildId),
                    cb.equal(rt.get(MessageEntry_.channelId), channelId))
        ).getResultList();

        return entries.isEmpty() ? null : entries.get(0);
    }

    public static Seq<MessageEntry> getMessageEntriesByGuild(long guildId) {
        List<MessageEntry> entries = Database.querySelect(MessageEntry.class, (cb, rt) ->
                cb.equal(rt.get(MessageEntry_.guildId), guildId)
        ).getResultList();

        return Seq.with(entries);
    }

    private static String cleanMessage(String string) {
        String lower = string.toLowerCase().replace('\n', ' ').trim();
        String noUrl = NO_URL_PATTERN.matcher(lower).replaceAll("");
        String cleanCopy = CLEAN_COPY_PATTERN.matcher(noUrl).replaceAll("").replace('.', ' ');
        String spaced = SPACED_PATTERN.matcher(cleanCopy).replaceAll(" ");

        return spaced;
    }
}