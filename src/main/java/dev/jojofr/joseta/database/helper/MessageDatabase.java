package dev.jojofr.joseta.database.helper;

import dev.jojofr.joseta.JosetaBot;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.BotDao;
import dev.jojofr.joseta.database.daos.MessageAttachmentDao;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.MessageAttachmentEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jdbi.v3.core.async.JdbiExecutor;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MessageDatabase {
    private static final int DB_WRITE_THREADS = 8;
    private static final int CONCURRENT_CHANNELS = 4;
    private static final int BATCH_SIZE = 500;

    public static CompletableFuture<Void> populateGuildMessages(Guild guild) { return populateGuildMessages(guild, false); }
    public static CompletableFuture<Void> populateGuildMessages(Guild guild, boolean missedOnly) {
        long start = System.nanoTime();
        String missed = missedOnly ? "missed " : "";
        Log.debug("Populating "+ missed +" messages table for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
        
        ExecutorService writeExecutor = Executors.newFixedThreadPool(DB_WRITE_THREADS);
        JdbiExecutor executor = JdbiExecutor.create(Database.get(), writeExecutor);
        
        Instant lastOnline = missedOnly ? Database.withExtension(BotDao.class, BotDao::getLastOnline) : null;
        return getGuildMessageChannels(guild).thenCompose(channels -> populateChannels(channels, executor, lastOnline))
            .whenComplete((totalCount, throwable) -> {
                writeExecutor.shutdown();
                if (throwable != null) Log.err("Failed to populate "+ missed + "messages table for guild: {} (ID: {})", throwable, guild.getName(), guild.getIdLong());
                else {
                    Log.debug("Populated "+ missed +"messages table with {} messages for guild: {} (ID: {})", totalCount, guild.getName(), guild.getIdLong());
                    Log.debug("Finished populating "+ missed +"messages table for guild: {} (ID: {}) in {} ms", guild.getName(), guild.getIdLong(), (System.nanoTime() - start) / 1_000_000.0);
                }
            }).thenAccept(ignore -> {});
    }
    
    public static CompletableFuture<Integer> addChannelMessageHistory(GuildMessageChannel channel, JdbiExecutor executor) { return addChannelMessageHistory(channel, executor, null); }
    public static CompletableFuture<Integer> addChannelMessageHistory(GuildMessageChannel channel, JdbiExecutor executor, Instant botLastOnline) {
        List<MessageEntity> buffer = new ArrayList<>(BATCH_SIZE);
        List<MessageAttachmentEntity> attachmentBuffer = new ArrayList<>(BATCH_SIZE);
        AtomicInteger count = new AtomicInteger(0);
        
        CompletableFuture<Void>[] writeChain = new CompletableFuture[]{CompletableFuture.completedFuture(null)};
        
        return channel.getIterableHistory().forEachAsync(message -> {
            if (botLastOnline != null && message.getTimeCreated().toInstant().isBefore(botLastOnline)) return false;
            
            MessageEntity entity = buildMessageEntity(message, false);
            
            if (!message.getAuthor().isBot() && !message.getAuthor().isSystem()) entity.markovContent = cleanContent(message.getContentRaw());
            
            buffer.add(entity);
            for (Message.Attachment attachment : message.getAttachments()) {
                MessageAttachmentEntity attachmentEntity = new MessageAttachmentEntity(message.getIdLong(), attachment);
                attachmentBuffer.add(attachmentEntity);
            }
            
            count.incrementAndGet();
            
            if (buffer.size() >= BATCH_SIZE) {
                List<MessageEntity> batch = new ArrayList<>(buffer);
                List<MessageAttachmentEntity> attachmentBatch = new ArrayList<>(attachmentBuffer);
                buffer.clear();
                attachmentBuffer.clear();
                
                writeChain[0] = writeChain[0].thenCompose(ign -> flushMessageBatchAsync(batch, attachmentBatch, executor));
            }
            
            return true;
        }).thenCompose(v -> {
            if (!buffer.isEmpty()){
                List<MessageEntity> batch = new ArrayList<>(buffer);
                List<MessageAttachmentEntity> attachmentBatch = new ArrayList<>(attachmentBuffer);
                buffer.clear();
                attachmentBuffer.clear();
                
                writeChain[0] = writeChain[0].thenCompose(ign -> flushMessageBatchAsync(batch, attachmentBatch, executor));
            }
            
            return writeChain[0];
        }).thenApply(v -> count.get());
    }
    
    private static CompletableFuture<Void> flushMessageBatchAsync(List<MessageEntity> batch, List<MessageAttachmentEntity> attachmentBatch, JdbiExecutor executor) {
        return executor.useTransaction(handle ->{
            handle.attach(MessageDao.class).upsertBatch(batch);
            
            if (!attachmentBatch.isEmpty()) handle.attach(MessageAttachmentDao.class).upsertAll(attachmentBatch);
        }).toCompletableFuture();
    }
    
    private static CompletableFuture<Integer> populateChannels(List<GuildMessageChannel> channels, JdbiExecutor executor, Instant lastOnline) {
        AtomicInteger next = new AtomicInteger();
        AtomicInteger total = new AtomicInteger();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>(Math.min(CONCURRENT_CHANNELS, channels.size()));
        for (int i = 0; i < Math.min(CONCURRENT_CHANNELS, channels.size()); i++)
            futures.add(populateNextChannel(channels, next, total, executor, lastOnline));
        
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(v -> total.get());
    }
    
    private static CompletableFuture<Void> populateNextChannel(List<GuildMessageChannel> channels, AtomicInteger next, AtomicInteger total, JdbiExecutor executor, Instant lastOnline) {
        int i = next.getAndIncrement();
        if (i >= channels.size()) return CompletableFuture.completedFuture(null);
        
        return addChannelMessageHistory(channels.get(i), executor, lastOnline)
            .thenAccept(total::addAndGet)
            .thenCompose(v -> populateNextChannel(channels, next, total, executor, lastOnline));
    }
    
    
    public static void addNewMessage(Message message) {
        MessageEntity messageEntity = buildMessageEntity(message);
        
        if (message.getAttachments().isEmpty()) {
            Database.useExtension(MessageDao.class, dao -> dao.upsert(messageEntity));
            return;
        }
        
        List<MessageAttachmentEntity> attachmentEntities = new ArrayList<>(message.getAttachments().size());
        for (Message.Attachment attachment : message.getAttachments()) {
            MessageAttachmentEntity attachmentEntity = new MessageAttachmentEntity(message.getIdLong(), attachment);
            attachmentEntities.add(attachmentEntity);
        }
        
        Database.useHandle(handle -> {
            handle.attach(MessageDao.class).upsert(messageEntity);
            handle.attach(MessageAttachmentDao.class).upsertAll(attachmentEntities);
        });
    }
    
    public static void updateMessage(Message message) {
        // Attachment can be updated/removed individually on a message
        List<MessageAttachmentEntity> attachmentEntities = new ArrayList<>(message.getAttachments().size());
        for (Message.Attachment attachment : message.getAttachments()) {
            MessageAttachmentEntity attachmentEntity = new MessageAttachmentEntity(message.getIdLong(), attachment);
            attachmentEntities.add(attachmentEntity);
        }
        
        Database.useHandle(handle -> {
            handle.attach(MessageDao.class).setContents(message.getIdLong(), message.getContentRaw(), cleanContent(message.getContentRaw()));
            
            MessageAttachmentDao attachmentDao = handle.attach(MessageAttachmentDao.class);
            attachmentDao.deleteByMessageId(message.getIdLong());
            attachmentDao.upsertAll(attachmentEntities);
        });
    }
    
    public static void deleteMessage(long messageId) {
        Database.useHandle(handle -> {
            handle.attach(MessageDao.class).delete(messageId);
            handle.attach(MessageAttachmentDao.class).deleteByMessageId(messageId);
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
    
    public static CompletableFuture<List<GuildMessageChannel>> getGuildMessageChannels(Guild guild) {
        List<GuildMessageChannel> channels = new ArrayList<>();
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
    
    public static final Pattern NO_MENTIONS_PATTERN = Pattern.compile("<@[!&]?\\d+>");
    public static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    // You may be like: "Oh, but why compile such simple regex?". Well, caching them is way more efficient than a replaceAll because said method recompiles it every time.
    private static final Pattern NO_SPACE_PATTERN = Pattern.compile("\\s+");
    
    private static String cleanContent(String content) {
        if (content == null || content.isEmpty()) return "";
        
        if (content.indexOf('<') >= 0)
            content = NO_MENTIONS_PATTERN.matcher(content).replaceAll("");
        if (content.contains("http") || content.contains("www."))
            content = NO_URL_PATTERN.matcher(content).replaceAll("");
        if (content.indexOf(' ') >= 0 || content.indexOf('\n') >= 0 || content.indexOf('\t') >= 0)
            content = NO_SPACE_PATTERN.matcher(content).replaceAll(" ");
        
        return content.trim();
    }
}
