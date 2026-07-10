package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.database.entities.MessageEntity_;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.hibernate.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessageDatabase {
    
    public static CompletableFuture<Void> populateNewGuild(Guild guild) {
        long start = System.nanoTime();
        Log.debug("Populating messages table for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
        
        ExecutorService writeExecutor = Executors.newVirtualThreadPerTaskExecutor();
        
        return getGuildMessageChannels(guild).thenCompose(channels -> {
            List<CompletableFuture<Integer>> futures = channels.stream()
                .map(channel -> addChannelMessageHistory(channel, guild, writeExecutor))
                .toList();
            
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream().mapToInt(CompletableFuture::join).sum());
        }).whenComplete((totalCount, throwable) -> {
            writeExecutor.shutdown();
            if (throwable != null) Log.err("Failed to populate messages table for guild: {} (ID: {})", throwable, guild.getName(), guild.getIdLong());
            else {
                Log.debug("Populated messages table with {} messages for guild: {} (ID: {})", totalCount, guild.getName(), guild.getIdLong());
                Log.debug("Finished populating messages table for guild: {} (ID: {}) in {} ms", guild.getName(), guild.getIdLong(), (System.nanoTime() - start) / 1_000_000.0);            }
        }).thenAccept(ignore -> {});
    }
    
    public static CompletableFuture<Integer> addChannelMessageHistory(GuildMessageChannel channel, Guild guild, ExecutorService writeExecutor) {
        List<MessageEntity> buffer = new ArrayList<>(500);
        List<CompletableFuture<Void>> pendingFlushes = new ArrayList<>();
        int[] count = {0};
        
        return channel.getIterableHistory().forEachAsync(message -> {
            MessageEntity entity;
            
            String content = message.getContentRaw();
            if (!content.isEmpty()) {
                String markovContent = null;
                if (!message.getAuthor().isBot() && !message.getAuthor().isSystem()) markovContent = cleanContent(message.getContentRaw());
                
                entity = new MessageEntity(
                    message.getIdLong(),
                    guild.getIdLong(),
                    channel.getIdLong(),
                    message.getAuthor().getIdLong(),
                    content,
                    markovContent,
                    message.getTimeCreated()
                );
            } else return true;
            
            buffer.add(entity);
            count[0]++;
            
            if (buffer.size() >= 500) pendingFlushes.add(flushBufferAsync(buffer, writeExecutor));
            
            return true;
        }).thenCompose(v -> {
            pendingFlushes.add(flushBufferAsync(buffer, writeExecutor));
            return CompletableFuture.allOf(pendingFlushes.toArray(CompletableFuture[]::new));
        }).thenApply(v -> count[0]).exceptionally(throwable -> {
            Log.err("Could not populate the messages table for channel ID: {} in guild: {} (ID: {}).", throwable, channel.getIdLong(), guild.getName(), guild.getIdLong());
            return count[0];
        });
    }
    
    private static CompletableFuture<Void> flushBufferAsync(List<MessageEntity> buffer, ExecutorService writeExecutor) {
        if (buffer.isEmpty()) return CompletableFuture.completedFuture(null);
        
        List<MessageEntity> toFlush = new ArrayList<>(buffer);
        buffer.clear();
        
        return CompletableFuture.runAsync(() -> {
            try (Session session = Database.getSession()) {
                Transaction transaction = session.beginTransaction();
                for (int i = 0; i < toFlush.size(); i++) {
                    session.persist(toFlush.get(i));
                    if (i % 50 == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                session.flush();
                transaction.commit();
            }
        }, writeExecutor);
    }
    
    public static void addNewMessage(Message message) {
        ConfigurationEntity config = BotCache.getGuildConfiguration(message.getGuild().getIdLong());
        addNewMessage(message, config.markovBlacklist);
    }
    
    public static void addNewMessage(Message message, Set<Long> markovBlackList) {
        String content = message.getContentRaw();
        if (content.isEmpty()) return;
        
        String markovContent = null;
        if (isMarkovEligible(message, markovBlackList))
            markovContent = cleanContent(content);
        
        Database.create(
            new MessageEntity(
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
        MessageEntity dbMessage = Database.get(MessageEntity.class, message.getIdLong());
        if (dbMessage == null) return;
        
        // If not null, then it is already eligible
        if (dbMessage.markovContent != null)
            dbMessage.setMarkovContent(cleanContent(message.getContentRaw()));
        
        Database.update(dbMessage.setContent(message.getContentRaw()));
    }
    
    public static void deleteMessage(long messageId) {
        MessageEntity dbMessage = Database.get(MessageEntity.class, messageId);
        if (dbMessage == null) return;
        
        Database.delete(dbMessage);
    }
    
    public static void deleteChannelMessages(long channelId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(MessageEntity.class, (cb, rt) -> cb.equal(rt.get(MessageEntity_.channelId), channelId), session).executeUpdate();
            tx.commit();
        }
    }
    
    public static void deleteGuildMessages(long guildId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(MessageEntity.class, (cb, rt) -> cb.equal(rt.get(MessageEntity_.guildId), guildId), session).executeUpdate();
            tx.commit();
        }
    }
    
    public static void deleteUserMarkovMessages(long userId) {
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryUpdate(MessageEntity.class,
                (cb, rt) -> cb.equal(rt.get(MessageEntity_.authorId), userId),
                (cb, rt) -> cb.set(rt.get(MessageEntity_.markovContent), (String) null),
                session
            ).executeUpdate();
            tx.commit();
        }
    }
    
    
    public static void updateMarkovEligibility(long guildId) {
        ConfigurationEntity config = BotCache.getGuildConfiguration(guildId);
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
                ScrollableResults<MessageEntity> results = session.createQuery(
                        "FROM MessageEntity WHERE guildId = :gid", MessageEntity.class)
                    .setParameter("gid", guildId)
                    .setCacheMode(CacheMode.IGNORE) // Important for batch performance
                    .scroll(ScrollMode.FORWARD_ONLY);
                
                while (results.next()) { try {
                    MessageEntity dbMessage = results.get();
                    
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
                } catch (Exception e) {
                    Log.err("Error processing message. Skipping to next message.", e);
                }}
                
                tx.commit();
                results.close();
            }
            
            Log.debug("Finished update. Updated {} messages for guild: {}", updatedCount, guild.getName());
        });
    }
    
    public static CompletableFuture<Set<GuildMessageChannel>> getGuildMessageChannels(Guild guild) {
        Set<GuildMessageChannel> channels = new HashSet<>();
        List<CompletableFuture<?>> pending = new ArrayList<>();
        
        for (GuildChannel channel : guild.getChannels()) {
            if (channel instanceof GuildMessageChannel messageChannel) channels.add(messageChannel);
            if (channel instanceof StandardGuildMessageChannel standardChannel) {
                channels.addAll(standardChannel.getThreadChannels());
                pending.add(standardChannel.retrieveArchivedPublicThreadChannels().submit().thenAccept(channels::addAll));
                pending.add(standardChannel.retrieveArchivedPrivateThreadChannels().submit().thenAccept(channels::addAll));
            }
        }
        
        return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).thenApply(v -> channels);
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
