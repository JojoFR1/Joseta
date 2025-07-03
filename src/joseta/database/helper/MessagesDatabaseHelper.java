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
import java.util.stream.*;

import org.hibernate.query.criteria.*;

import jakarta.persistence.criteria.*;

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
            // TODO alternative for the hardcoded 10000 limit?
            for (Message message : channel.getIterableHistory().takeAsync(10000).thenApply(list -> list.stream().collect(Collectors.toList())).get()) {                
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
        
        Databases.getInstance().create(
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
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<MessageEntry> query = criteriaBuilder.createQuery(MessageEntry.class);
        Root<MessageEntry> root = query.from(MessageEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.messageId), messageId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.channelId), channelId));
        query.select(root).where(where);

        MessageEntry entry = Databases.getInstance().getSession()
            .createSelectionQuery(query)
            .getResultList().get(0);
        
        Databases.getInstance().createOrUpdate(entry.setContent(content));
    }

    public static void deleteMessage(long messageId, long guildId, long channelId) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaDelete<MessageEntry> query = criteriaBuilder.createCriteriaDelete(MessageEntry.class);
        Root<MessageEntry> root = query.from(MessageEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.messageId), messageId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.channelId), channelId));
        query.where(where);

        Databases.getInstance().getSession().createMutationQuery(query).executeUpdate();
    }

    public static void deleteChannelMessages(long guildId, long channelId) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaDelete<MessageEntry> query = criteriaBuilder.createCriteriaDelete(MessageEntry.class);
        Root<MessageEntry> root = query.from(MessageEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.channelId), channelId));
        query.where(where);

        Databases.getInstance().getSession().createMutationQuery(query).executeUpdate();

    }

    public static MessageEntry getMessageEntry(long messageId, long guildId, long channelId) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<MessageEntry> query = criteriaBuilder.createQuery(MessageEntry.class);
        Root<MessageEntry> root = query.from(MessageEntry.class);
        Predicate where = criteriaBuilder.conjunction();
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.messageId), messageId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.guildId), guildId));
        where = criteriaBuilder.and(where, criteriaBuilder.equal(root.get(MessageEntry_.channelId), channelId));
        query.select(root).where(where);

        MessageEntry entry = Databases.getInstance().getSession()
            .createSelectionQuery(query)
            .getResultList().get(0);
        
        return entry;
    }

    public static Seq<MessageEntry> getMessageEntries(long guildId) {
        HibernateCriteriaBuilder criteriaBuilder = Databases.getInstance().getCriteriaBuilder();
        CriteriaQuery<MessageEntry> query = criteriaBuilder.createQuery(MessageEntry.class);
        Root<MessageEntry> root = query.from(MessageEntry.class);
        Predicate where = criteriaBuilder.equal(root.get(MessageEntry_.guildId), guildId);
        query.select(root).where(where);

        List<MessageEntry> entries = Databases.getInstance().getSession()
            .createSelectionQuery(query)
            .getResultList();

        return Seq.with(entries);
    }
}