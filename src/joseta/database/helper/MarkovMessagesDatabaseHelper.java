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
import java.util.stream.*;

import org.hibernate.query.criteria.*;

import jakarta.persistence.criteria.*;

/** Copy of {@link MarkovMessagesDatabaseHelper} but for the special Markov messages */
public class MarkovMessagesDatabaseHelper {
    private static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    private static final Pattern CLEAN_COPY_PATTERN = Pattern.compile("[^a-z0-9.?!,;\\-()~\"'&$€£\\]\\[àáâãäåæçèéêëìíîïñòóôõöùúûüýÿ ]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPACED_PATTERN = Pattern.compile("\\s+");
    
    public static void populateNewGuild(Guild guild) {
        int count = 0;
        Log.debug("Populating the Messages Database...");

        ConfigEntry config = Database.get(ConfigEntry.class, guild.getIdLong());
        
        for (TextChannel channel : guild.getTextChannels()) {
            Seq<Long> markovBlackList = config.getMarkovBlackList();
            if (channel.isNSFW()
                || markovBlackList.contains(channel.getIdLong())
                || markovBlackList.contains(channel.getParentCategoryIdLong())
            ) continue;

            for (ThreadChannel thread : channel.getThreadChannels()) count += addChannelMessageHistory(thread, guild);
            count += addChannelMessageHistory(channel, guild);
        }

        Log.debug("Populated Markov Messages Database with "+ count +" messages for guild: " + guild.getName() + " (" + guild.getId() + ")");
    }

    private static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild) {
        int count = 0;
        try {
            // TODO alternative for the hardcoded 10000 limit?
            for (Message message : channel.getIterableHistory().takeAsync(10000).thenApply(list -> list.stream().collect(Collectors.toList())).get()) {
                addNewMessage(message, guild, channel, message.getAuthor().getIdLong(), message.getContentRaw(), message.getTimeCreated().toInstant().toString());
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

        ConfigEntry config = Database.get(ConfigEntry.class, guildId);
        
        Seq<Long> markovBlackList = config.getMarkovBlackList();
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return;
        if (markovBlackList.contains(channel.getIdLong())
            || markovBlackList.contains(message.getAuthor().getIdLong())) return;

        if (message.getMember() != null && markovBlackList.containsAll(Seq.with(message.getMember().getRoles()).map(role -> role.getIdLong()))) return;
        
            
        if (channel instanceof TextChannel textChannel &&
            (textChannel.isNSFW() || markovBlackList.contains(textChannel.getParentCategoryIdLong()))) return;
        
        Database.createOrUpdate(
            new MarkovMessageEntry(
                id,
                guildId,
                channel.getIdLong(),
                authorId,
                cleanMessage(content),
                Instant.parse(timestamp)
            )
        );
    }

    public static void updateMessage(long messageId, long guildId, long channelId, String content) {
        if (getMessageEntry(messageId, guildId, channelId) == null) return; // Entry does not exist, no need to update
        
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaQuery<MarkovMessageEntry> query = criteriaBuilder.createQuery(MarkovMessageEntry.class);
        Root<MarkovMessageEntry> root = query.from(MarkovMessageEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MarkovMessageEntry_.messageId), messageId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MarkovMessageEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MarkovMessageEntry_.channelId), channelId));
        query.select(root).where(where);

        MarkovMessageEntry entry = Database.getSession()
            .createSelectionQuery(query)
            .getResultList().get(0);

        Database.createOrUpdate(entry.setContent(cleanMessage(content)));
    }

    public static void deleteMessage(long messageId, long guildId, long channelId) {
        if (getMessageEntry(messageId, guildId, channelId) == null) return; // Entry does not exist, no need to delete

        Database.queryDelete(MarkovMessageEntry.class, (cb, rt) ->
                cb.and(cb.equal(rt.get(MarkovMessageEntry_.messageId), messageId),
                        cb.equal(rt.get(MarkovMessageEntry_.guildId), guildId),
                        cb.equal(rt.get(MarkovMessageEntry_.channelId), channelId))
        ).executeUpdate();
    }

    public static void deleteChannelMessages(long guildId, long channelId) {
        if (getMessageEntriesByChannel(guildId, channelId).isEmpty()) return; // No entries to delete

        Database.queryDelete(MarkovMessageEntry.class, (cb, rt) ->
                cb.and(cb.equal(rt.get(MarkovMessageEntry_.guildId), guildId),
                        cb.equal(rt.get(MarkovMessageEntry_.channelId), channelId))
        ).executeUpdate();
    }

    public static MarkovMessageEntry getMessageEntry(long messageId, long guildId, long channelId) {
         List<MarkovMessageEntry> entries = Database.querySelect(MarkovMessageEntry.class, (cb, rt) ->
                 cb.and(cb.equal(rt.get(MarkovMessageEntry_.messageId), messageId),
                         cb.equal(rt.get(MarkovMessageEntry_.guildId), guildId),
                         cb.equal(rt.get(MarkovMessageEntry_.channelId), channelId))
         ).getResultList();

         return entries.isEmpty() ? null : entries.get(0);
    }

    public static Seq<MarkovMessageEntry> getMessageEntriesByChannel(long guildId, long channelId) {
        List<MarkovMessageEntry> entries = Database.querySelect(MarkovMessageEntry.class, (cb, rt) ->
                cb.and(cb.equal(rt.get(MarkovMessageEntry_.guildId), guildId),
                        cb.equal(rt.get(MarkovMessageEntry_.channelId), channelId))
        ).getResultList();

        return Seq.with(entries);
    }

    public static Seq<MarkovMessageEntry> getMessageEntriesByGuild(long guildId) {
        List<MarkovMessageEntry> entries = Database.querySelect(MarkovMessageEntry.class, (cb, rt) ->
                cb.equal(rt.get(MarkovMessageEntry_.guildId), guildId)
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