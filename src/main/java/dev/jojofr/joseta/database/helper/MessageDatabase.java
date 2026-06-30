package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MessageDatabase {
    
    public static void populateNewGuild(Guild guild) {
        int count = 0;
        Log.debug("Populating messages table for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
        for (GuildChannel channel : guild.getChannels()) {
            ConfigurationEntity config = BotCache.getGuildConfiguration(guild.getIdLong());
            
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
        ConfigurationEntity config = BotCache.getGuildConfiguration(message.getGuild().getIdLong());
        addNewMessage(message, config.markovBlacklist);
    }
    
    public static void addNewMessage(Message message, Set<Long> markovBlackList) {
        String content = message.getContentRaw();
        if (content.isEmpty()) return;
        
        String markovContent;
        if (isMarkovEligible(message, markovBlackList)) markovContent = cleanContent(content);
        else  markovContent = null;
        
        Database.useHandle(handle -> handle.attach(MessageDao.class).upsert(
            new MessageEntity(
                message.getIdLong(),
                message.getGuild().getIdLong(),
                message.getChannel().getIdLong(),
                message.getAuthor().getIdLong(),
                content,
                markovContent,
                message.getTimeCreated()
            )
        ));
    }
    
    public static void updateMessage(Message message) {
        MessageEntity dbMessage = Database.withHandle(handle -> handle.attach(MessageDao.class).getById(message.getIdLong()));
        if (dbMessage == null) return;
        
        // If not null, then it is already eligible
        if (dbMessage.markovContent != null)
            dbMessage.setMarkovContent(cleanContent(message.getContentRaw()));
        
        Database.useHandle(handle -> handle.attach(MessageDao.class).upsert(dbMessage.setContent(message.getContentRaw())));
    }
    
    public static void deleteMessage(long messageId) {
        MessageEntity dbMessage = Database.withHandle(handle -> handle.attach(MessageDao.class).getById(messageId));
        if (dbMessage == null) return;
        
        Database.useHandle(handle -> handle.attach(MessageDao.class).delete(messageId));
    }
    
    public static void updateMarkovEligibility(long guildId) {
        ConfigurationEntity config = BotCache.getGuildConfiguration(guildId);
        updateMarkovEligibility(guildId, config.markovBlacklist);
    }
    
    public static void updateMarkovEligibility(long guildId, Set<Long> markovBlacklist) {
        CompletableFuture.runAsync(() -> {
            Guild guild = JosetaBot.get().getGuildById(guildId);
            if (guild == null) return;
        
            Log.debug("Updating markov eligibility for guild: {} (ID: {})", guild.getName(), guildId);
            
            int updatedCount = Database.withHandle(handle -> {
                MessageDao messageDao = handle.attach(MessageDao.class);
                
                final PreparedBatch batch = handle.prepareBatch("UPDATE messages SET markov_content = :markovContent WHERE id = :id");
                AtomicInteger batchSize = new AtomicInteger();
                AtomicInteger updateCount = new AtomicInteger();
                
                try (Stream<MessageEntity> results = messageDao.getByGuildId(guildId)) {
                    results.forEach(dbMessage -> {
                        try {
                            boolean isEligible = isDatabaseMarkovEligible(guild, dbMessage, markovBlacklist);
                            String newMarkovContent = isEligible ? cleanContent(dbMessage.content) : null;
                            
                            if (dbMessage.markovContent.equals(newMarkovContent)) return;
                            
                            
                            batch.bind("id", dbMessage.id)
                                .bind("markovContent", dbMessage.markovContent)
                                .add();
                            
                            updateCount.incrementAndGet();
                            if (batchSize.getAndIncrement() >= 500) {
                                batch.execute();
                                batchSize.set(0);
                            }
                        } catch (Exception e) {
                            Log.err("Error processing message.", e);
                        }
                    });
                }
                
                if (batchSize.get() > 0) batch.execute();
                
                return updateCount.get();
            });
            
            Log.debug("Finished update. Updated {} messages for guild: {}", updatedCount, guild.getName());
        });
    }
    
    private static boolean isDatabaseMarkovEligible(Guild guild, MessageEntity dbMessage, Set<Long> markovBlacklist) {
        if (markovBlacklist.contains(dbMessage.authorId)) return false;
        
        Member member = guild.getMemberById(dbMessage.authorId);
        if (member != null && member.getUnsortedRoles().stream().anyMatch(role -> markovBlacklist.contains(role.getIdLong()))) return false;
        
        GuildChannel channel = guild.getGuildChannelById(dbMessage.channelId);
        // Very likely an archived thread, less likely a deleted channel. Can't check, assume ineligible.
        if (channel == null) return  false;
        
        if (markovBlacklist.contains(channel.getIdLong())
            || (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW())
            || (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0 && markovBlacklist.contains(categorizableChannel.getParentCategoryIdLong()))
        ) return false;
        
        return true;
    }

    private static boolean isMarkovEligible(Message message, Set<Long> markovBlacklist) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return false;
        if (markovBlacklist.contains(message.getAuthor().getIdLong())) return false;
        
        Member member =  message.getMember();
        if (member != null && member.getUnsortedRoles().stream().anyMatch(role -> markovBlacklist.contains(role.getIdLong()))) return false;
        
        GuildMessageChannel channel = message.getGuildChannel();
        if (markovBlacklist.contains(channel.getIdLong())
            || (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW())
            || (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0 && markovBlacklist.contains(categorizableChannel.getParentCategoryIdLong()))
        ) return false;
        
        return true;
    }
    
    // You may be like: "Oh, but why compile such simple regex?". Well, caching them is way more efficient than a replaceAll because said method recompiles it every time.
    private static final Pattern NO_SPACE_PATTERN = Pattern.compile("\\s+");
    public static final Pattern NO_MENTIONS_PATTERN = Pattern.compile("<@[!&]?\\d+>");
    public static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    
    private static String cleanContent(String content) {
        String noMentions = NO_MENTIONS_PATTERN.matcher(content).replaceAll("");
        String noUrl = NO_URL_PATTERN.matcher(noMentions).replaceAll("");
        
        return NO_SPACE_PATTERN.matcher(noUrl.trim()).replaceAll(" ");
    }
}
