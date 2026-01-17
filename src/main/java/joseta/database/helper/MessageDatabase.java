package joseta.database.helper;

import joseta.JosetaBot;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.entities.Message_;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Set;
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
            Set<Long> markovBlackList = config.markovBlackList;
            
            if (!(channel instanceof GuildMessageChannel messageChannel)) continue;
            count += addChannelMessageHistory(messageChannel, guild, markovBlackList);
            
            if (channel instanceof StandardGuildMessageChannel standardChannel) {
                for (ThreadChannel thread : standardChannel.getThreadChannels())
                    count += addChannelMessageHistory(thread, guild, markovBlackList);
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
    
    
    private static boolean isMarkovEligible(Message message, Set<Long> markovBlackList) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return false;
        if (message.getChannel() instanceof StandardGuildMessageChannel messageChannel
            && (messageChannel.isNSFW() || markovBlackList.contains(messageChannel.getIdLong()) || markovBlackList.contains(messageChannel.getParentCategoryIdLong()))) return false;
        else if (message.getChannel() instanceof ThreadChannel threadChannel && threadChannel.getParentChannel() instanceof StandardGuildMessageChannel parentChannel
            && (parentChannel.isNSFW() || markovBlackList.contains(parentChannel.getIdLong()) || markovBlackList.contains(parentChannel.getParentCategoryIdLong()))) return false;
        if (markovBlackList.contains(message.getAuthor().getIdLong()) || message.getMember().getUnsortedRoles().stream().map(role -> role.getIdLong()).anyMatch(markovBlackList::contains)) return false;
        
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
