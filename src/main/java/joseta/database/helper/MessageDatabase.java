package joseta.database.helper;

import joseta.JosetaBot;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.entities.Message_;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MessageDatabase {
    private static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    private static final Pattern CLEAN_COPY_PATTERN = Pattern.compile("[^a-z0-9.?!,;\\-()~\"'&$€£\\]\\[àáâãäåæçèéêëìíîïñòóôõöùúûüýÿ ]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPACED_PATTERN = Pattern.compile("\\s+");
    
    public static void populateNewGuild(Guild guild) {
        int count = 0;
        Log.debug("Populating messages table for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
        for (GuildChannel channel : guild.getChannels()) {
            Configuration config = Database.get(Configuration.class, guild.getIdLong());
            
            if (!(channel instanceof GuildMessageChannel messageChannel)) continue;
            count += addChannelMessageHistory(messageChannel, guild, config.markovBlacklist);
            
            if (channel instanceof StandardGuildMessageChannel standardChannel) {
                for (ThreadChannel thread : standardChannel.getThreadChannels())
                    count += addChannelMessageHistory(thread, guild, config.markovBlacklist);
            }
        }
        
        Log.debug("Populated messages table with {} messages for guild: {} (ID: {})", count, guild.getName(), guild.getIdLong());
    }
    
    public static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild, Set<Long> markovBlackList) {
        int count = 0;
        try {
            for (Message message : channel.getIterableHistory().stream().toList()) {
                addNewMessage(message, markovBlackList);
                count++;
            }
        } catch (Exception e) {
            Log.err("Could not populate the messages table for channel ID: {} in guild: {} (ID: {}).", e, channel.getIdLong(), guild.getName(), guild.getIdLong());
        }
        
        return count;
    }
    
    public static void addNewMessage(Message message) {
        Configuration config = Database.get(Configuration.class, message.getGuild().getIdLong());
        addNewMessage(message, config.markovBlacklist);
    }
    
    public static void addNewMessage(Message message, Set<Long> markovBlackList) {
        String content = message.getContentRaw();
        if (content.isEmpty()) return;
        
        String markovContent = null;
        if (isMarkovEligible(message, markovBlackList))
            markovContent = cleanContent(content);
        
        Database.create(
            new joseta.database.entities.Message(
                message.getIdLong(),
                message.getGuild().getIdLong(),
                message.getChannel().getIdLong(),
                message.getAuthor().getIdLong(),
                content,
                markovContent,
                message.getTimeCreated()
            )
        );
    }
    
    public static void updateMessage(Message message) {
        joseta.database.entities.Message dbMessage = Database.get(joseta.database.entities.Message.class, message.getIdLong());
        if (dbMessage == null) return;
        
        // If not null, then it is already eligible
        if (dbMessage.markovContent != null)
            dbMessage.setMarkovContent(cleanContent(message.getContentRaw()));
        
        Database.update(dbMessage.setContent(message.getContentRaw()));
    }
    
    public static void deleteMessage(long messageId) {
        joseta.database.entities.Message dbMessage = Database.get(joseta.database.entities.Message.class, messageId);
        if (dbMessage == null) return;
        
        Database.delete(dbMessage);
    }
    
    public static void deleteChannelMessages(long channelId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(joseta.database.entities.Message.class, (cb, rt) -> cb.equal(rt.get(Message_.channelId), channelId), session).executeUpdate();
            tx.commit();
        }
    }
    
    public static void deleteGuildMessages(long guildId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(joseta.database.entities.Message.class, (cb, rt) -> cb.equal(rt.get(Message_.guildId), guildId), session).executeUpdate();
            tx.commit();
        }
    }
    
    
    public static void updateMarkovEligibility(long guildId) {
        Configuration config = Database.get(Configuration.class, guildId);
        updateMarkovEligibility(guildId, config.markovBlacklist);
    }
    
    public static void updateMarkovEligibility(long guildId, Set<Long> markovBlacklist) {
        CompletableFuture.runAsync(() -> {
            Guild guild = JosetaBot.get().getGuildById(guildId);
            if (guild == null) return;
        
            Log.debug("Updating markov eligibility for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
            
            int updatedCount = 0;
            try (Session session = Database.getSession()) {
                Transaction tx = session.beginTransaction();
                
                // Allow to process large datasets without loading everything into memory
                ScrollableResults<joseta.database.entities.Message> results = session.createQuery(
                        "FROM Message WHERE guildId = :gid", joseta.database.entities.Message.class)
                    .setParameter("gid", guildId)
                    .setCacheMode(org.hibernate.CacheMode.IGNORE) // Important for batch performance
                    .scroll(ScrollMode.FORWARD_ONLY);
                
                while (results.next()) {
                    joseta.database.entities.Message dbMessage = results.get();
                    
                    boolean isEligible = isDatabaseMarkovEligible(guild, dbMessage, markovBlacklist);
                    boolean changed = false;
                    
                    if (!isEligible && dbMessage.markovContent != null) {
                        dbMessage.setMarkovContent(null);
                        changed = true;
                    } else if (isEligible && dbMessage.markovContent == null) {
                        dbMessage.setMarkovContent(cleanContent(dbMessage.content));
                        changed = true;
                    }
                    
                    if (changed) {
                        session.merge(dbMessage);
                        updatedCount++;
                    }
                    
                    // Periodic flush and clear to manage memory
                    if (updatedCount % 50 == 0 && changed) {
                        session.flush();
                        session.clear();
                    }
                }
                
                tx.commit();
            }
            
            Log.debug("Finished update. Updated {} messages for guild: {}", updatedCount, guild.getName());
        });
    }
    
    private static boolean isDatabaseMarkovEligible(Guild guild, joseta.database.entities.Message dbMessage, Set<Long> markovBlacklist) {
        if (markovBlacklist.contains(dbMessage.authorId)) return false;
        
        Member member = guild.getMemberById(dbMessage.authorId);
        if (member != null && member.getUnsortedRoles().stream().anyMatch(role -> markovBlacklist.contains(role.getIdLong()))) return false;
        
        GuildChannel channel = guild.getGuildChannelById(dbMessage.channelId);
        StandardGuildMessageChannel messageChannel = null;
        if (channel instanceof StandardGuildMessageChannel standardChannel) messageChannel = standardChannel;
        else if (channel instanceof ThreadChannel threadChannel && threadChannel.getParentChannel() instanceof StandardGuildMessageChannel parent) messageChannel = parent;
        
        if (messageChannel.isNSFW() || markovBlacklist.contains(messageChannel.getIdLong())
            || (messageChannel.getParentCategoryIdLong() != 0 && markovBlacklist.contains(messageChannel.getParentCategoryIdLong()))) return false;
        
        return true;
    }

    private static boolean isMarkovEligible(Message message, Set<Long> markovBlacklist) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return false;
        if (markovBlacklist.contains(message.getAuthor().getIdLong())) return  false;
        
        Member member =  message.getMember();
        if (member != null && member.getUnsortedRoles().stream().anyMatch(role -> markovBlacklist.contains(role.getIdLong()))) return false;
        
        GuildChannel channel = message.getGuildChannel();
        StandardGuildMessageChannel messageChannel = null;
        if (channel instanceof StandardGuildMessageChannel standardChannel) messageChannel = standardChannel;
        else if (channel instanceof ThreadChannel threadChannel && threadChannel.getParentChannel() instanceof StandardGuildMessageChannel parent) messageChannel = parent;
        
        if (messageChannel.isNSFW() || markovBlacklist.contains(messageChannel.getIdLong())
            || (messageChannel.getParentCategoryIdLong() != 0 && markovBlacklist.contains(messageChannel.getParentCategoryIdLong()))) return false;
        
        return true;
    }
    
    private static String cleanContent(String content) {
        String lower = content.toLowerCase().replace('\n', ' ').trim();
        String noUrl = NO_URL_PATTERN.matcher(lower).replaceAll("");
        String cleanCopy = CLEAN_COPY_PATTERN.matcher(noUrl).replaceAll("").replace('.', ' ');
        String spaced = SPACED_PATTERN.matcher(cleanCopy).replaceAll(" ");
        
        return spaced;
    }
}
