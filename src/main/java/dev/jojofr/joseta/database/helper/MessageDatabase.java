package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        
        return CompletableFuture.runAsync(() ->
            Database.useHandle(handle -> handle.attach(MessageDao.class).upsertBatch(toFlush)),
            writeExecutor
        );
    }
    
    private static MessageEntity buildMessageEntity(Message message) {
        String content = message.getContentRaw();
        if (content.isEmpty()) return null;
        
        String markovContent;
        if (isMarkovEligible(message)) markovContent = cleanContent(content);
        else markovContent = null;
        
        return new MessageEntity(
            message.getIdLong(),
            message.getGuild().getIdLong(),
            message.getChannel().getIdLong(),
            message.getAuthor().getIdLong(),
            content,
            markovContent,
            message.getTimeCreated()
        );
    }
    
    private static void flush(List<MessageEntity> buffer) {
        if (buffer.isEmpty()) return;
        Database.useHandle(handle -> handle.attach(MessageDao.class).upsertBatch(buffer));
        buffer.clear();
    }
    
    public static void addNewMessage(Message message) {
        Database.useHandle(handle -> handle.attach(MessageDao.class).upsert(buildMessageEntity(message)));
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
                            boolean isEligible = isDatabaseMarkovEligible(guild, dbMessage);
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
    
    private static boolean isDatabaseMarkovEligible(Guild guild, MessageEntity dbMessage) {
        return Database.withHandle(handle -> {
            MessageDao.MarkovBlacklistDao markovBlacklistDao = handle.attach(MessageDao.MarkovBlacklistDao.class);
            
            if (markovBlacklistDao.isIdBlacklisted(guild.getIdLong(), dbMessage.authorId)) return false;
            
            Member member = guild.getMemberById(dbMessage.authorId);
            if (member == null) return false;
            
            Set<Long> roleIds = member.getUnsortedRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toSet());
            if (!roleIds.isEmpty() && markovBlacklistDao.isAnyIdBlacklisted(guild.getIdLong(), roleIds)) return false;
            
            GuildChannel channel = guild.getGuildChannelById(dbMessage.channelId);
            // Very likely an archived thread, less likely a deleted channel. Can't check, assume ineligible.
            if (channel == null) return  false;
            
            if (markovBlacklistDao.isIdBlacklisted(guild.getIdLong(), channel.getIdLong())) return false;
            if (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW()) return false;
            if (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0
                && markovBlacklistDao.isIdBlacklisted(guild.getIdLong(), categorizableChannel.getParentCategoryIdLong())) return false;
            
            return true;
        });
    }

    private static boolean isMarkovEligible(Message message) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return false;
        
        return Database.withHandle(handle -> {
            MessageDao.MarkovBlacklistDao markovBlacklistDao = handle.attach(MessageDao.MarkovBlacklistDao.class);
            
            if (markovBlacklistDao.isIdBlacklisted(message.getGuild().getIdLong(), message.getAuthor().getIdLong())) return false;
            
            Member member =  message.getMember();
            if (member == null) return false;
            
            Set<Long> roleIds = member.getUnsortedRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toSet());
            if (!roleIds.isEmpty() && markovBlacklistDao.isAnyIdBlacklisted(message.getGuild().getIdLong(), roleIds)) return false;
            
            GuildMessageChannel channel = message.getGuildChannel();
            
            
            if (markovBlacklistDao.isIdBlacklisted(message.getGuild().getIdLong(), channel.getIdLong())) return false;
            if (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW()) return false;
            if (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0
                && markovBlacklistDao.isIdBlacklisted(message.getGuild().getIdLong(), categorizableChannel.getParentCategoryIdLong())) return false;
            
            return true;
        });
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
