package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jdbi.v3.core.async.JdbiExecutor;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageDatabase {
    
    public static CompletableFuture<Void> populateNewGuild(Guild guild) {
        long start = System.nanoTime();
        Log.debug("Populating messages table for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
        
        ExecutorService writeExecutor = Executors.newVirtualThreadPerTaskExecutor();
        JdbiExecutor executor = JdbiExecutor.create(Database.get(), writeExecutor);
        
        return getGuildMessageChannels(guild).thenCompose(channels -> {
            List<CompletableFuture<Integer>> futures = channels.stream()
                .map(channel -> addChannelMessageHistory(channel, guild, executor))
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
    
    public static CompletableFuture<Integer> addChannelMessageHistory(GuildMessageChannel channel, Guild guild, JdbiExecutor executor) {
        List<MessageEntity> buffer = new ArrayList<>(500);
        List<CompletableFuture<Void>> pendingFlushes = new ArrayList<>();
        int[] count = {0};
        
        return channel.getIterableHistory().forEachAsync(message -> {
            MessageEntity entity = buildMessageEntity(message, false);
            if (entity == null) return true;
            
            if (!message.getAuthor().isBot() && !message.getAuthor().isSystem()) entity.markovContent = cleanContent(message.getContentRaw());
            
            buffer.add(entity);
            count[0]++;
            
            if (buffer.size() >= 500) pendingFlushes.add(flushBufferAsync(buffer, executor));
            
            return true;
        }).thenCompose(v -> {
            pendingFlushes.add(flushBufferAsync(buffer, executor));
            return CompletableFuture.allOf(pendingFlushes.toArray(CompletableFuture[]::new));
        }).thenApply(v -> count[0]).exceptionally(throwable -> {
            Log.err("Could not populate the messages table for channel ID: {} in guild: {} (ID: {}).", throwable, channel.getIdLong(), guild.getName(), guild.getIdLong());
            return count[0];
        });
    }
    
    private static CompletableFuture<Void> flushBufferAsync(List<MessageEntity> buffer, JdbiExecutor executor) {
        if (buffer.isEmpty()) return CompletableFuture.completedFuture(null);
        
        List<MessageEntity> toFlush = new ArrayList<>(buffer);
        buffer.clear();
        
        return executor.useExtension(MessageDao.class, dao -> dao.upsertBatch(toFlush)).exceptionally(throwable -> {
            Log.err("Failed to flush message buffer to database.", throwable);
            return null;
        }).toCompletableFuture();
    }
    
    public static void addNewMessage(Message message) {
        MessageEntity messageEntity = buildMessageEntity(message);
        if (messageEntity == null) return;
        
        Database.useExtension(MessageDao.class, dao -> dao.upsert(messageEntity));
    }
    
    public static void updateMessage(Message message) {
        Database.useHandle(handle -> {
            MessageDao messageDao = handle.attach(MessageDao.class);
            
            MessageEntity dbMessage = messageDao.getById(message.getIdLong());
            if (dbMessage == null) return;
            
            // If not null, then it is already eligible
            if (dbMessage.markovContent != null)
                dbMessage.setMarkovContent(cleanContent(message.getContentRaw()));
            
            messageDao.upsert(dbMessage.setContent(message.getContentRaw()));
        });
    }
    
    public static void deleteMessage(long messageId) {
        Database.useHandle(handle -> {
            MessageDao messageDao = handle.attach(MessageDao.class);
            
            MessageEntity dbMessage = messageDao.getById(messageId);
            if (dbMessage == null) return;
            
            messageDao.delete(messageId);
        });
    }
    
    public static void updateMarkovEligibility(long guildId) {
        CompletableFuture.runAsync(() -> {
            Guild guild = JosetaBot.get().getGuildById(guildId);
            if (guild == null) return;
            
            Set<Long> markovBlacklist = BotCache.getGuildConfiguration(guildId).markovBlacklistIds;
            Log.debug("Updating Markov eligibility for guild: {} (ID: {})", guild.getName(), guildId);
            
            int updatedCount = Database.withHandle(handle -> {
                MessageDao messageDao = handle.attach(MessageDao.class);
                
                PreparedBatch batch = handle.prepareBatch("UPDATE messages SET markov_content = :markovContent WHERE id = :id");
                int updateCount = 0;
                
                try (Stream<MessageEntity> results = messageDao.getByGuildId(guildId)) {
                    for (MessageEntity dbMessage : (Iterable<MessageEntity>) results::iterator) {
                        try {
                            boolean isEligible = isDatabaseMarkovEligible(guild, dbMessage, markovBlacklist);
                            String newMarkovContent = isEligible ? cleanContent(dbMessage.content) : null;
                            
                            if (Objects.equals(dbMessage.markovContent, newMarkovContent)) continue;
                            
                            batch.bind("id", dbMessage.id)
                                .bind("markovContent", newMarkovContent)
                                .add();
                            
                            updateCount++;
                            if (batch.size() >= 500) batch.execute();
                        } catch (Exception e) {
                            Log.err("Error processing message.", e);
                        }
                    }
                }
                if (batch.size() > 0) batch.execute();
                
                return updateCount;
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
    
    private static MessageEntity buildMessageEntity(Message message) { return buildMessageEntity(message, true); }
    private static MessageEntity buildMessageEntity(Message message, boolean checkMarkov) {
        String content = message.getContentRaw();
        if (content.isEmpty()) return null;
        
        String markovContent = null;
        if (checkMarkov && isMarkovEligible(message, BotCache.getGuildConfiguration(message.getGuild().getIdLong()).markovBlacklistIds))
            markovContent = cleanContent(content);
        
        return new MessageEntity(
            message.getIdLong(),
            message.getGuild().getIdLong(),
            message.getChannel().getIdLong(),
            message.getAuthor().getIdLong(),
            content,
            markovContent,
            message.getTimeCreated().toInstant()
        );
    }
    
    private static boolean isDatabaseMarkovEligible(Guild guild, MessageEntity dbMessage, Set<Long> markovBlacklist) {
        if (markovBlacklist.contains(dbMessage.authorId)) return false;
        
        GuildChannel channel = guild.getGuildChannelById(dbMessage.channelId);
        // Very likely an archived thread, less likely a deleted channel. Can't check, assume ineligible.
        if (channel == null) return  false;
        
        if (markovBlacklist.contains(channel.getIdLong())) return false;
        if (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW()) return false;
        if (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0
            && markovBlacklist.contains(categorizableChannel.getParentCategoryIdLong())) return false;
        
        Member member = guild.getMemberById(dbMessage.authorId);
        if (member != null)
            for (Role role : member.getUnsortedRoles()) if (markovBlacklist.contains(role.getIdLong())) return false;
        
        return true;
    }

    private static boolean isMarkovEligible(Message message, Set<Long> markovBlacklist) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return false;
        if (markovBlacklist.contains(message.getAuthor().getIdLong())) return false;
        
        GuildChannel channel = message.getGuildChannel();
        if (markovBlacklist.contains(channel.getIdLong()))  return false;
        if (channel instanceof IAgeRestrictedChannel ageRestrictedChannel && ageRestrictedChannel.isNSFW()) return false;
        if (channel instanceof ICategorizableChannel categorizableChannel && categorizableChannel.getParentCategoryIdLong() != 0
            && markovBlacklist.contains(categorizableChannel.getParentCategoryIdLong())) return false;
        
        Member member = message.getMember();
        if (member != null)
            for (Role role : member.getUnsortedRoles()) if (markovBlacklist.contains(role.getIdLong())) return false;
        
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
